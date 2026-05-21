package com.howprom.admin.dto;

import com.howprom.common.entity.Problem;
import com.howprom.common.entity.EvaluationType; // Enum import 필요
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
    
    // Enum을 문자열로 노출하기 위해 String 유지
    private String evaluationType; 
    
    private Integer tokenLimit;
    private Float correctnessWeight;
    private Float efficiencyWeight;
    private Float avgUserTokens;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private List<RequirementAdminDTO> requirements; 

    public static ProblemAdminDTO from(Problem problem) {
        if (problem == null) return null;
        
        return ProblemAdminDTO.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .exampleInput(problem.getExampleInput())
                .exampleOutput(problem.getExampleOutput())
                // Enum 객체에서 이름을 문자열로 추출 (null 체크 추가)
                .evaluationType(problem.getEvaluationType() != null ? problem.getEvaluationType().name() : null)
                .tokenLimit(problem.getTokenLimit())
                .correctnessWeight(problem.getCorrectnessWeight())
                .efficiencyWeight(problem.getEfficiencyWeight())
                .avgUserTokens(problem.getAvgUserTokens())
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