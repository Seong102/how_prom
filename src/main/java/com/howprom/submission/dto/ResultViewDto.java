package com.howprom.submission.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResultViewDto {
    private Long    problemId;
    private String  problemTitle;
    private String  evaluationType;
    private Integer score;
    private String  status;
    private Integer totalUserTokens;
    private Integer turnCount;
    
    private Integer llmScore;        // Σ requirementsResult[].score
    private Double  correctnessPart; // llmScore × 0.7
    private Double  efficiencyPart;  // score - correctnessPart
    private Float   avgUserTokens;   // problems.avg_user_tokens
}
