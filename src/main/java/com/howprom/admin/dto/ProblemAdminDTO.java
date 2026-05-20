package com.howprom.admin.dto;

import com.howprom.common.entity.Problem;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemAdminDTO {
    private Long id;
    private String title;
    private String description;
    private String exampleInput;
    private String exampleOutput;
    private Integer difficulty;
    private String evaluationType;
    private Integer tokenLimit;
    private Float correctnessWeight;
    private Float efficiencyWeight;
    private Float avgPromptTokens;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProblemAdminDTO from(Problem problem) {
        if (problem == null) return null;
        
        return ProblemAdminDTO.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .exampleInput(problem.getExampleInput())
                .exampleOutput(problem.getExampleOutput())
                .difficulty(problem.getDifficulty())
                .evaluationType(problem.getEvaluationType())
                .tokenLimit(problem.getTokenLimit())
                .correctnessWeight(problem.getCorrectnessWeight())
                .efficiencyWeight(problem.getEfficiencyWeight())
                .avgPromptTokens(problem.getAvgPromptTokens())
                .isPublic(problem.getIsPublic())
                .createdAt(problem.getCreatedAt())
                .updatedAt(problem.getUpdatedAt())
                .build();
    }
}