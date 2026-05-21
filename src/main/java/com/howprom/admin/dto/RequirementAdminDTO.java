package com.howprom.admin.dto;

import com.howprom.common.entity.Requirement;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequirementAdminDTO {
    private String description; // content에서 변경
    private Integer weight;

    // 엔티티를 DTO로 안전하게 변환
    public static RequirementAdminDTO from(Requirement requirement) {
        if (requirement == null) return null;
        
        return RequirementAdminDTO.builder()
                .description(requirement.getDescription())
                .weight(requirement.getWeight())
                .build();
    }
}