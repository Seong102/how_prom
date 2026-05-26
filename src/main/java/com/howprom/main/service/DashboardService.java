package com.howprom.main.service;

import com.howprom.main.dto.TopSubmissionRow;
import com.howprom.main.dto.ContinueProblemRow;
import com.howprom.common.entity.Problem;
import com.howprom.common.entity.EvaluationType;
import com.howprom.repository.ProblemRepository;
import com.howprom.repository.SubmissionRepository;
import com.howprom.repository.RequirementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    /** 새로 등록된 문제: 최근 7일 이내, 최대 5개 */
    private static final int NEW_PROBLEMS_DAYS = 7;
    private static final int NEW_PROBLEMS_LIMIT = 5;
    private static final int TOP_SUBMISSIONS_LIMIT = 5;

    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;
    private final RequirementRepository requirementRepository;
    
    /**
     * 새로 등록된 문제 (최근 7일, 최대 5개)
     * 비로그인/로그인 동일하게 사용
     */
    public List<Map<String, Object>> getNewProblems() {
        LocalDateTime since = LocalDateTime.now().minusDays(NEW_PROBLEMS_DAYS);
        List<Problem> problems = problemRepository
                .findByIsPublicTrueAndCreatedAtAfterOrderByCreatedAtDesc(
                        since, PageRequest.of(0, NEW_PROBLEMS_LIMIT));

        return problems.stream()
                .map(this::toNewProblemMap)
                .toList();
    }

    private Map<String, Object> toNewProblemMap(Problem p) {
        LocalDate createdDate = p.getCreatedAt().toLocalDate();
        Map<String, Object> m = new HashMap<>();
        m.put("problemId", p.getId());
        m.put("title", p.getTitle());
        m.put("evaluationType", p.getEvaluationType().name());
        m.put("createdAt", createdDate);
        m.put("relativeDate", formatRelativeDate(createdDate));
        return m;
    }

    private String formatRelativeDate(LocalDate date) {
        long days = ChronoUnit.DAYS.between(date, LocalDate.now());
        if (days == 0) return "오늘";
        if (days == 1) return "어제";
        return days + "일 전";
    }
    
    
    /**
     * 이번 주(월~일) 우수 풀이 — PASSED 상위
     *
     * @param viewerId 조회자 ID, 비로그인 시 null
     */
    public List<Map<String, Object>> getTopWeeklySubmissions(Long viewerId) {
        LocalDateTime weekStart = startOfThisWeek();
        LocalDateTime weekEnd   = weekStart.plusDays(7);

        // 비로그인일 땐 -1L로 두면, solved 판정은 항상 false가 나옴
        long viewer = (viewerId == null) ? -1L : viewerId;

        List<TopSubmissionRow> rows = submissionRepository.findTopWeeklySubmissions(
                weekStart, weekEnd, viewer,
                PageRequest.of(0, TOP_SUBMISSIONS_LIMIT));

        return rows.stream().map(this::toTopSubmissionMap).toList();
    }

    private Map<String, Object> toTopSubmissionMap(TopSubmissionRow r) {
        Map<String, Object> m = new HashMap<>();
        m.put("submissionId", r.getSubmissionId());
        m.put("problemId", r.getProblemId());
        m.put("problemTitle", r.getProblemTitle());
        m.put("nickname", r.getNickname());
        m.put("score", r.getScore());
        m.put("solved", r.getSolved());
        return m;
    }

    /** 이번 주 월요일 00:00 */
    private LocalDateTime startOfThisWeek() {
        LocalDate today = LocalDate.now();
        int dayOfWeek = today.getDayOfWeek().getValue(); // 월=1, ..., 일=7
        LocalDate monday = today.minusDays(dayOfWeek - 1L);
        return monday.atStartOfDay();
    }
    
    
    /**
     * 대시보드 추천 문제 — 평가 유형(STANDARD/EFFICIENCY/BUDGET)별로 안 푼 문제 중 최신 1개씩
     *
     * @param userId 로그인 사용자 ID
     */
    public List<Map<String, Object>> getRecommendedProblems(Long userId) {
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        for (EvaluationType type : EvaluationType.values()) {
            List<Problem> found = problemRepository.findRecommendedByType(
                    type, userId, PageRequest.of(0, 1));

            if (!found.isEmpty()) {
                result.add(toRecommendedMap(found.get(0)));
            }
        }
        return result;
    }

    private Map<String, Object> toRecommendedMap(Problem p) {
        long requirementCount = requirementRepository.countByProblemId(p.getId());

        Map<String, Object> m = new HashMap<>();
        m.put("problemId", p.getId());
        m.put("title", p.getTitle());
        m.put("evaluationType", p.getEvaluationType().name());
        m.put("requirementCount", requirementCount);

        // EFFICIENCY일 때만 avgUserTokens 표시
        if (p.getEvaluationType() == EvaluationType.EFFICIENCY) {
            // FLOAT을 정수로 반올림해서 표시
            int avg = Math.round(p.getAvgUserTokens());
            m.put("avgUserTokens", avg > 0 ? avg : null);  // 0이면 데이터 없음 → null
        } else {
            m.put("avgUserTokens", null);
        }

        // BUDGET일 때만 tokenLimit 표시
        if (p.getEvaluationType() == EvaluationType.BUDGET) {
            m.put("tokenLimit", p.getTokenLimit());
        } else {
            m.put("tokenLimit", null);
        }

        return m;
    }
    
    /**
     * 이어서 풀기 — 가장 최근에 시도했지만 아직 PASSED 못 받은 문제
     *
     * @param userId 로그인 사용자 ID
     * @return Map (없으면 null)
     */
    public Map<String, Object> getContinueProblem(Long userId) {
        List<ContinueProblemRow> rows = submissionRepository.findContinueProblem(
                userId, PageRequest.of(0, 1));

        if (rows.isEmpty()) {
            return null;   // HTML에서 빈 상태 UI로 처리
        }

        ContinueProblemRow r = rows.get(0);
        Map<String, Object> m = new HashMap<>();
        m.put("problemId", r.getProblemId());
        m.put("title", r.getTitle());
        m.put("evaluationType", r.getEvaluationType().name());
        m.put("lastStatus", r.getLastStatus().name());
        m.put("lastScore", r.getLastScore());
        m.put("lastSubmittedAt", r.getLastSubmittedAt());
        return m;
    }
}