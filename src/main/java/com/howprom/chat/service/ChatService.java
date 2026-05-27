package com.howprom.chat.service;

import com.howprom.chat.dto.ChatMessageRequest;
import com.howprom.chat.dto.ChatMessageResponse;
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

    public ChatMessageResponse chat(ChatMessageRequest request) {

        // 1. messages 배열 구성 — system prompt 고정값만 사용
        //    문제 설명은 사용자가 직접 입력 (자동 포함 X)
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
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

        // 7. 이번 턴 user 토큰 수 계산
        //    prompt_eval_count = system + 전체 대화 이력 + 이번 user 메시지 누적값
        //    이번 턴 user 토큰 = prompt_eval_count - systemPromptTokens - prevTotalTokens
        int prevTotalTokens  = request.getPrevTotalTokens() != null ? request.getPrevTotalTokens() : 0;
        int thisUserTokens   = extractThisUserTokens(llmResponse, prevTotalTokens);
        int assistantTokens  = extractAssistantTokens(llmResponse); // AI 답변 토큰 (프론트 누적용)

        log.info("[ChatService] prompt_eval_count에서 계산한 이번 턴 토큰: {}, assistant 토큰: {}",
                thisUserTokens, assistantTokens);

        return new ChatMessageResponse(content, thisUserTokens, assistantTokens);
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> response) {
        Map<String, Object> message = (Map<String, Object>) response.get("message");
        return (String) message.get("content");
    }

    private int extractAssistantTokens(Map<String, Object> response) {
        // eval_count = AI가 생성한 답변 토큰 수
        Object count = response.get("eval_count");
        if (count == null) return 0;
        return ((Number) count).intValue();
    }

    private int extractThisUserTokens(Map<String, Object> response, int prevTotalTokens) {
        Object count = response.get("prompt_eval_count");
        if (count == null) return 0;
        int totalInput = ((Number) count).intValue();
        log.info("[ChatService] prompt_eval_count={}, systemPromptTokens={}, prevTotalTokens={}",
                totalInput, systemPromptTokens, prevTotalTokens);
        // 이번 턴 user 토큰 = 전체입력 - system prompt 토큰 - 이전 누적 토큰
        int thisUserTokens = totalInput - systemPromptTokens - prevTotalTokens;
        return Math.max(0, thisUserTokens);
    }
}