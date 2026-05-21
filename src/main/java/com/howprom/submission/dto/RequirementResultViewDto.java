package com.howprom.submission.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RequirementResultViewDto {
    private String  description;
    private Integer score;
    private Integer maxScore;
    private Integer pct;
}
