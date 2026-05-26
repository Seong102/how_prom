package com.howprom.main.dto;

import com.howprom.common.entity.EvaluationType;
import com.howprom.common.entity.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 대시보드 "이어서 풀기" 영역용 DTO
 */
@Getter
@AllArgsConstructor
public class ContinueProblemRow {
    private Long problemId;
    private String title;
    private EvaluationType evaluationType;
    private SubmissionStatus lastStatus;
    private Integer lastScore;
    private LocalDateTime lastSubmittedAt;
}