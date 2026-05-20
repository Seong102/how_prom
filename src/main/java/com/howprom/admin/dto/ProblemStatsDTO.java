package com.howprom.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class ProblemStatsDTO {
    private Long problemId;
    private Long totalCount;
    private Double avgScore;
    // 필요에 따라 passed_count, avg_tokens 등 추가 가능
}
