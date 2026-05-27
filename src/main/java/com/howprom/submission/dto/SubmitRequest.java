package com.howprom.submission.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class SubmitRequest {
    private Long problemId;
    private List<ChatMessageDto> conversation;  // 전체 대화 이력
    private Integer totalUserTokens;            // 사용자 턴 토큰 누적합
    private String finalCode;                   // 에디터에 작성된 최종 코드
}