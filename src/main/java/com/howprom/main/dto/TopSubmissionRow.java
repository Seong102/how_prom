package com.howprom.main.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 대시보드 "이번 주 우수 풀이" 영역용 DTO
 *
 * solved: 조회자가 해당 문제를 PASSED로 푼 적 있는지
 *   - true  → "풀이 보기" 버튼 (outline)
 *   - false → "풀러 가기" 버튼 (fill)
 */
@Getter
@AllArgsConstructor
public class TopSubmissionRow {
    private Long submissionId;
    private Long problemId;
    private String problemTitle;
    private String nickname;
    private Integer score;
    private Boolean solved;
}