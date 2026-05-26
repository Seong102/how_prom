package com.howprom.problem.service;

import com.howprom.common.entity.EvaluationType;
import com.howprom.common.entity.SubmissionStatus;
import com.howprom.problem.dto.ProblemListItem;
import com.howprom.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemListService {

    /** 페이지당 표시 개수 — 변경 시 이 상수만 수정 */
    public static final int PAGE_SIZE = 20;

    private final ProblemRepository problemRepository;

    /**
     * 문제 목록 조회
     *
     * @param userId         로그인 사용자 ID, 비로그인 시 null
     * @param evaluationType STANDARD/EFFICIENCY/BUDGET, null이면 전체
     * @param statusFilter   ALL / NOT_SOLVED / PASSED_ONLY (로그인 사용자만 의미 있음)
     * @param sort           "latest" (기본) / "oldest"
     * @param page           0부터 시작하는 페이지 번호
     */
    public Page<ProblemListItem> getList(
            Long userId,
            EvaluationType evaluationType,
            String statusFilter,
            String sort,
            int page) {

        Pageable pageable = PageRequest.of(page, PAGE_SIZE, buildSort(sort));

        if (userId == null) {
            // 비로그인 — 필터는 evaluationType만 적용 (status 필터는 의미 없음)
            return problemRepository.findPublicListForGuest(evaluationType, pageable);
        }

        // 로그인 사용자
        String evalParam  = (evaluationType == null) ? null : evaluationType.name();
        String statusParam = (statusFilter == null) ? "ALL" : statusFilter;

        Page<Object[]> raw = problemRepository.findPublicListForUser(
                userId, evalParam, statusParam, pageable);

        return raw.map(this::mapRowToDto);
    }

    private Sort buildSort(String sort) {
        boolean oldest = "oldest".equalsIgnoreCase(sort);
        return Sort.by(oldest ? Sort.Direction.ASC : Sort.Direction.DESC,
                "createdAt");
    }

    /**
     * Native query 결과(Object[])를 DTO로 변환
     * 컬럼 순서: id, title, evaluationType, createdAt, status, bestScore
     */
    private ProblemListItem mapRowToDto(Object[] row) {
        Long id = ((Number) row[0]).longValue();
        String title = (String) row[1];

        EvaluationType evalType = EvaluationType.valueOf((String) row[2]);

        // LocalDateTime / Timestamp 둘 다 안전하게 처리
        LocalDateTime createdAt;
        if (row[3] instanceof LocalDateTime ldt) {
            createdAt = ldt;
        } else if (row[3] instanceof java.sql.Timestamp ts) {
            createdAt = ts.toLocalDateTime();
        } else {
            createdAt = null;  // 방어적 처리
        }

        SubmissionStatus status = (row[4] == null) ? null : SubmissionStatus.valueOf((String) row[4]);
        Integer bestScore = (row[5] == null) ? null : ((Number) row[5]).intValue();

        return new ProblemListItem(id, title, evalType, createdAt, status, bestScore);
    }
}