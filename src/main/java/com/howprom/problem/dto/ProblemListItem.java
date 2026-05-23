package com.howprom.problem.dto;

import com.howprom.common.entity.EvaluationType;
import com.howprom.common.entity.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 문제 목록 화면용 DTO
 * - 비로그인: status, bestScore가 null로 채워짐 (화면에서 컬럼 자체 숨김)
 * - 로그인:
 *     · PASSED 받은 적 있음 → status=PASSED, bestScore=최고점수
 *     · FAILED/GRADING/ERROR만 있음 → status=가장 최근 상태, bestScore=null
 *     · 제출 이력 없음 → status=null, bestScore=null
 */
@Getter
@AllArgsConstructor
public class ProblemListItem {
    private Long id;
    private String title;
    private EvaluationType evaluationType;
    private LocalDateTime createdAt;
    private SubmissionStatus status;   // 로그인 사용자의 풀이 상태 (null=미도전 또는 비로그인)
    private Integer bestScore;         // PASSED 받은 적 있을 때만 채워짐
}