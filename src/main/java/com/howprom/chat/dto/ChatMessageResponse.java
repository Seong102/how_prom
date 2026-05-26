package com.howprom.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMessageResponse {
    private String content;       // AI 응답 텍스트
    private int promptTokens;     // 확정 토큰 수 (프론트 게이지 보정용)
}