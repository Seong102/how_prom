package com.howprom.domain.submission.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor  // JSON 역직렬화를 위해 기본 생성자 필수
@AllArgsConstructor
public class RequirementResultDto {
    private Long id;         // 요구사항 고유 ID [cite: 54]
    private Integer score;   // 해당 항목 채점 점수 [cite: 54]
    private String comment;  // 달성 여부에 대한 LLM의 세부 평평 [cite: 54]
}