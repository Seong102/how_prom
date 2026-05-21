package com.howprom.admin.dto;

import com.howprom.common.entity.Requirement;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequirementAdminDTO {
    private String content;
    private Integer weight;

    // 엔티티를 DTO로 안전하게 변환
    public static RequirementAdminDTO from(Requirement requirement) {
        if (requirement == null) return null;
        
        return RequirementAdminDTO.builder()
                .content(requirement.getContent())
                .weight(requirement.getWeight())
                .build();
    }
}