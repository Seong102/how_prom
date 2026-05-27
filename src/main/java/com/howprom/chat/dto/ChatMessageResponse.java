package com.howprom.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMessageResponse {
    private String content;          // AI 응답 텍스트
    private int promptTokens;        // 이번 턴 user 토큰 수
    private int assistantTokens;     // AI 답변 토큰 수 (프론트에서 prevTotalTokens 누적용)
}