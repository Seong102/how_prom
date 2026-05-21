package com.howprom.submission.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor  // JSON 역직렬화를 위해 기본 생성자 필수
@AllArgsConstructor
public class ChatMessageDto {
    private String role;     // user (사용자) 또는 assistant (AI 답변) [cite: 100]
    private String content;  // 대화 내용 본문 [cite: 147]
    private Integer tokens;  // 해당 턴에서 소모된 실제 토큰 수 [cite: 160]
}