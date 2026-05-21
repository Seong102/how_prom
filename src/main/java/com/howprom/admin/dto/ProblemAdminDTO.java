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
    private String evaluationType;
    private Integer tokenLimit;
    private Float correctnessWeight;
    private Float efficiencyWeight;
    private Float avgUserTokens;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 다른 DTO를 리스트로 포함
    private List<RequirementAdminDTO> requirements; 

    public static ProblemAdminDTO from(Problem problem) {
        if (problem == null) return null;
        
        return ProblemAdminDTO.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .exampleInput(problem.getExampleInput())
                .exampleOutput(problem.getExampleOutput())
                .evaluationType(problem.getEvaluationType())
                .tokenLimit(problem.getTokenLimit())
                .correctnessWeight(problem.getCorrectnessWeight())
                .efficiencyWeight(problem.getEfficiencyWeight())
                .avgUserTokens(problem.getAvgUserTokens()) // 🔄 [변경] 변경된 엔티티 메서드 호출
                .isPublic(problem.getIsPublic())
                .createdAt(problem.getCreatedAt())
                .updatedAt(problem.getUpdatedAt())
                .requirements(problem.getRequirements() != null ? 
                        problem.getRequirements().stream()
                                .map(RequirementAdminDTO::from)
                                .collect(Collectors.toList()) : new ArrayList<>())
                .build();
    }
}