package com.howprom.chat.dto;
 
import lombok.Getter;
import java.util.List;
 
@Getter
public class ChatMessageRequest {
    private Long problemId;
    private List<MessageDto> messages;
    private Integer prevTotalTokens;  // 이전 턴까지의 누적 사용자 토큰 (첫 턴은 0)
 
    @Getter
    public static class MessageDto {
        private String role;    // "user" | "assistant"
        private String content;
    }
}
 