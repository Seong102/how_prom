package com.howprom.community.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CommunityListDto {
    private Long submissionId;
    private String nickname;
    private Integer score;
    private Integer totalUserTokens;
    private LocalDateTime submittedAt;
}
