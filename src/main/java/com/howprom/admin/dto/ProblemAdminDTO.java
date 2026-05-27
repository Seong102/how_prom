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
    private List<RequirementAdminDTO> requirements;

    public int getRequirementsCount() {
        return this.requirements != null ? this.requirements.size() : 0;
    }

    // 7번 수정: @Transactional 컨텍스트 안에서만 호출되도록 주석 명시
    // requirements는 반드시 트랜잭션 내부에서 fetch된 상태로 전달받아야 함
    public static ProblemAdminDTO from(Problem problem) {
        if (problem == null) return null;

        // 5번 수정: boolean 타입이므로 isPublic() 으로 호출
        return ProblemAdminDTO.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .exampleInput(problem.getExampleInput())
                .exampleOutput(problem.getExampleOutput())
                .evaluationType(problem.getEvaluationType() != null
                        ? problem.getEvaluationType().name() : null)
                .tokenLimit(problem.getTokenLimit())
                .correctnessWeight(problem.getCorrectnessWeight())
                .efficiencyWeight(problem.getEfficiencyWeight())
                .avgUserTokens(problem.getAvgUserTokens())
                .isPublic(problem.isPublic())   // 5번 수정: getIsPublic() → isPublic()
                .createdAt(problem.getCreatedAt())
                .updatedAt(problem.getUpdatedAt())
                .requirements(problem.getRequirements() != null ?
                        problem.getRequirements().stream()
                                .map(RequirementAdminDTO::from)
                                .collect(Collectors.toList()) : new ArrayList<>())
                .build();
    }
}