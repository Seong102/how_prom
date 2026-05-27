package com.howprom.submission.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.howprom.common.entity.Requirement;
import com.howprom.submission.dto.RequirementResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmGradingService {

    @Value("${llm.base-url}")
    private String baseUrl;

    @Value("${llm.model}")
    private String model;

    @Value("${llm.timeout-seconds:120}")
    private int timeoutSeconds;

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * LLM #2 채점 호출
     * 반환: { score, passed, feedback, requirements: [{id, score, comment}] }
     */
    public GradingResult grade(String problemTitle, String problemDescription,
                               List<Requirement> requirements, String finalCode) {

        // 1. 요구사항 목록을 프롬프트용 텍스트로 변환
        StringBuilder reqText = new StringBuilder();
        for (Requirement r : requirements) {
            reqText.append("- [ID:").append(r.getId()).append("] (").append(r.getWeight())
                   .append("점) ").append(r.getDescription()).append("\n");
        }

        // 2. 채점 전용 system prompt
        String systemPrompt = """
                당신은 Java 코딩 문제 채점 전문가입니다.
                반드시 아래 규칙을 따르세요:
                1. 오직 아래 JSON 형식만 반환하세요. 설명, 인사, 마크다운 코드블록 일체 금지.
                2. 이전 대화나 다른 문제는 완전히 무시하고, 지금 주어진 문제와 요구사항만 기준으로 채점하세요.
                3. 각 요구사항은 코드에 해당 로직이 명확히 존재하면 weight 전체 점수, 없으면 0점으로만 채점하세요.
                4. score는 requirements의 score 합산값이어야 합니다.
                5. score >= 60이면 passed=true, 미만이면 false입니다.
                반환 형식:
                {"score":전체점수,"passed":true또는false,"feedback":"피드백","requirements":[{"id":요구사항ID,"score":달성점수,"comment":"코멘트"}]}
                """;

        // 3. 채점 요청 user 메시지
        String userPrompt = String.format("""
                [채점 대상 문제]
                문제명: %s
                문제 설명: %s
                
                [요구사항 목록 - 이 요구사항만 기준으로 채점]
                %s
                
                [제출된 Java 코드]
                %s
                
                위 코드를 위 요구사항 기준으로만 채점하여 JSON을 반환하세요.
                이전 문제나 다른 기준은 무시하세요.
                """, problemTitle, problemDescription, reqText.toString(), finalCode);

        // 4. Ollama 호출 — 채점 전용 메시지만 전달 (이전 대화 이력 제외)
        //    이전 대화 이력을 포함하면 LLM이 이전 문제 문맥으로 채점하는 오류 발생
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user",   "content", userPrompt));

        /* Ollama options — 채점은 결정적으로 (temperature=0) */
        Map<String, Object> options = new HashMap<>();
        options.put("temperature", 0.0);   /* 편차 최소화 — 같은 코드는 항상 같은 점수 */
        options.put("seed", 42);           /* 시드 고정으로 재현성 확보 */

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("stream", false);
        body.put("options", options);
        body.put("keep_alive", 0);         /* 요청 후 모델 세션 즉시 해제 — 컨텍스트 오염 방지 */

        WebClient client = webClientBuilder
                .baseUrl(baseUrl)
                .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();

        // 5. 1회 시도 + 실패 시 재시도 1회
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                Map<String, Object> llmResponse = client.post()
                        .uri("/api/chat")
                        .header("Content-Type", "application/json")
                        .bodyValue(body)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .timeout(Duration.ofSeconds(timeoutSeconds))
                        .block();

                // 6. 응답 파싱
                @SuppressWarnings("unchecked")
                Map<String, Object> message = (Map<String, Object>) llmResponse.get("message");
                String rawJson = (String) message.get("content");

                // JSON만 추출 (```json ... ``` 감싸진 경우 대비)
                String cleanJson = extractJson(rawJson);

                return parseGradingResult(cleanJson);

            } catch (Exception e) {
                log.error("LLM 채점 호출 실패 (attempt {}): {}", attempt, e.getMessage());
                if (attempt == 2) {
                    // 2회 실패 → ERROR 처리용 기본값 반환
                    return GradingResult.error();
                }
            }
        }
        return GradingResult.error();
    }

    /** JSON 블록 추출 (```json ... ``` 또는 ``` ... ``` 제거) */
    private String extractJson(String raw) {
        if (raw == null) return "{}";
        String s = raw.trim();
        if (s.startsWith("```")) {
            s = s.replaceFirst("```json", "").replaceFirst("```", "").trim();
            int end = s.lastIndexOf("```");
            if (end != -1) s = s.substring(0, end).trim();
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    private GradingResult parseGradingResult(String json) throws Exception {
        Map<String, Object> map = objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));

        String feedback = (String) map.getOrDefault("feedback", "");

        List<RequirementResultDto> reqResults = new ArrayList<>();
        List<Map<String, Object>> reqs = (List<Map<String, Object>>) map.get("requirements");
        if (reqs != null) {
            for (Map<String, Object> r : reqs) {
                Long   id      = ((Number) r.get("id")).longValue();
                int    rScore  = ((Number) r.getOrDefault("score", 0)).intValue();
                String comment = (String) r.getOrDefault("comment", "");
                reqResults.add(new RequirementResultDto(id, rScore, comment));
            }
        }

        // 총점은 LLM 응답값을 쓰지 않고 requirements 점수 합산으로 직접 계산
        // → LLM이 임의로 총점을 조정하는 오류 방지
        int score = reqResults.stream()
                .mapToInt(RequirementResultDto::getScore)
                .sum();
        score = Math.max(0, Math.min(100, score)); // 0~100 범위 보정

        boolean passed = score >= 60;

        return new GradingResult(score, passed, feedback, reqResults, false);
    }

    /* ── 채점 결과 래퍼 ── */
    public static class GradingResult {
        private final int score;
        private final boolean passed;
        private final String feedback;
        private final List<RequirementResultDto> requirementsResult;
        private final boolean isError;

        public GradingResult(int score, boolean passed, String feedback,
                             List<RequirementResultDto> requirementsResult, boolean isError) {
            this.score = score;
            this.passed = passed;
            this.feedback = feedback;
            this.requirementsResult = requirementsResult;
            this.isError = isError;
        }

        public int score()                              { return score; }
        public boolean passed()                         { return passed; }
        public String feedback()                        { return feedback; }
        public List<RequirementResultDto> requirementsResult() { return requirementsResult; }
        public boolean isError()                        { return isError; }

        public static GradingResult error() {
            return new GradingResult(0, false, "", List.of(), true);
        }
    }
}