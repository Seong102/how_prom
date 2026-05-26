package com.howprom.chat.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class ChatMessageRequest {
    private Long problemId;
    private List<MessageDto> messages;

    @Getter
    public static class MessageDto {
        private String role;    // "user" | "assistant"
        private String content;
    }
}