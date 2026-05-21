package com.howprom.admin.dto;

import com.howprom.common.entity.Problem;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    
    // 💡 다른 DTO를 깔끔하게 리스트로 포함시킵니다.
    private List<RequirementAdminDTO> requirements; 

    public static ProblemAdminDTO from(Problem problem) {
        if (problem == null) return null;
        
        return ProblemAdminDTO.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .exampleInput(problem.getExampleInput())
                .exampleOutput(problem.getExampleOutput())
                //.difficulty(problem.getDifficulty())
                .evaluationType(problem.getEvaluationType())
                .tokenLimit(problem.getTokenLimit())
                .correctnessWeight(problem.getCorrectnessWeight())
                .efficiencyWeight(problem.getEfficiencyWeight())
                //.avgPromptTokens(problem.getAvgPromptTokens())
                .isPublic(problem.getIsPublic())
                .createdAt(problem.getCreatedAt())
                .updatedAt(problem.getUpdatedAt())
                // 💡 Requirement 엔티티 리스트를 RequirementAdminDTO 리스트로 변환하여 조립
                .requirements(problem.getRequirements() != null ? 
                        problem.getRequirements().stream()
                                .map(RequirementAdminDTO::from)
                                .collect(Collectors.toList()) : new ArrayList<>())
                .build();
    }
}