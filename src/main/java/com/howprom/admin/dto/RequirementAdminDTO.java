package com.howprom.admin.dto;

import com.howprom.common.entity.Requirement;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequirementAdminDTO {
    
    // 🔥 HTML 하단 테이블에서 요구하는 필드들 대거 추가
    private Long probId;         // ${req.probId}
    private String title;        // ${req.title}
    private String description;  // ${req.description}
    private Integer weight;      // ${req.weight}
    private String avgAchieve;   // ${req.avgAchieve} (예: "85%")
    private String failRate;     // ${req.failRate}   (예: "15%")
    private String color;        // ${req.color}      (예: "#ff0000")

    // 엔티티를 DTO로 안전하게 변환 (연관된 Problem 엔티티 등에서 ID와 제목을 가져와야 합니다)
    public static RequirementAdminDTO from(Requirement requirement) {
        if (requirement == null) return null;
        
        return RequirementAdminDTO.builder()
                // 예시: Requirement 엔티티 구조에 따라 다를 수 있으므로 프로젝트에 맞게 매핑하세요.
                .probId(requirement.getProblem() != null ? requirement.getProblem().getId() : null)
                .title(requirement.getProblem() != null ? requirement.getProblem().getTitle() : "알 수 없는 문제")
                .description(requirement.getDescription())
                .weight(requirement.getWeight())
                .avgAchieve("0%")  // 통계 계산 로직 필요 시 추후 연동
                .failRate("0%")    // 통계 계산 로직 필요 시 추후 연동
                .color("#4A90E2")  // 기본 컬러값 지정
                .build();
    }
}