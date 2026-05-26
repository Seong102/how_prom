package com.howprom.chat.service;

import com.howprom.chat.dto.ChatMessageRequest;
import com.howprom.chat.dto.ChatMessageResponse;
import com.howprom.common.entity.Problem;
import com.howprom.repository.ProblemRepository;
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
public class ChatService {

    @Value("${llm.base-url}")
    private String baseUrl;

    @Value("${llm.model}")
    private String model;

    @Value("${llm.system-prompt}")
    private String systemPrompt;

    @Value("${llm.system-prompt-tokens}")
    private int systemPromptTokens;

    @Value("${llm.timeout-seconds:120}")
    private int timeoutSeconds;

    private final WebClient.Builder webClientBuilder;
    private final ProblemRepository problemRepository;

    public ChatMessageResponse chat(ChatMessageRequest request) {

        // 1. 문제 조회
        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));

        // 2. system prompt에 문제 내용 포함
        String fullSystemPrompt = systemPrompt
                + "\n\n현재 문제: " + problem.getTitle()
                + "\n" + problem.getDescription();

        // 3. messages 배열 구성
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", fullSystemPrompt));
        for (ChatMessageRequest.MessageDto msg : request.getMessages()) {
            messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }

        // 4. 요청 body 구성
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("stream", false);

        // 5. Ollama 호출
        WebClient client = webClientBuilder
                .baseUrl(baseUrl)
                .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();

        Map<String, Object> llmResponse = client.post()
                .uri("/api/chat")
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .doOnError(e -> log.error("Ollama 호출 실패: {}", e.getMessage()))
                .block();

        // 6. 응답 파싱
        String content = extractContent(llmResponse);
        int userPromptTokens = extractUserTokens(llmResponse);

        return new ChatMessageResponse(content, userPromptTokens);
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> response) {
        Map<String, Object> message = (Map<String, Object>) response.get("message");
        return (String) message.get("content");
    }

    private int extractUserTokens(Map<String, Object> response) {
        // prompt_eval_count = 전체 입력 토큰 수
        // system prompt 토큰을 빼면 사용자 순수 토큰 수
        Object count = response.get("prompt_eval_count");
        if (count == null) return 0;
        return Math.max(0, ((Number) count).intValue() - systemPromptTokens);
    }
}