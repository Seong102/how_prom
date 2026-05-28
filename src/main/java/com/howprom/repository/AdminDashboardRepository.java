package com.howprom.repository;

import com.howprom.common.entity.Submission;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminDashboardRepository extends JpaRepository<Submission, Long> {

    @Query("SELECT COUNT(s) FROM Submission s")
    Long countTotalSubmissions();

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.submittedAt >= :startOfToday")
    Long countTodaySubmissions(@Param("startOfToday") LocalDateTime startOfToday);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startOfToday")
    Long countTodayJoinCount(@Param("startOfToday") LocalDateTime startOfToday);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.status = 'GRADING'")
    Long countGradingCount();

    @Query("SELECT COUNT(u) FROM User u")
    Long countTotalUserCount();

    @Query("SELECT COALESCE((SUM(CASE WHEN s.status = 'PASSED' THEN 1.0 ELSE 0.0 END) / COUNT(s)) * 100, 0.0) FROM Submission s")
    Double calculateTotalPassRate();

    @Query("SELECT COUNT(DISTINCT s.user.id) FROM Submission s")
    Long countActiveUsers();

    @Query("SELECT COUNT(p) FROM Problem p WHERE p.isPublic = true")
    Long countPublicProblems();

    @Query("SELECT COUNT(p) FROM Problem p")
    Long countTotalProblems();

    @Query(value = "SELECT COALESCE(AVG(TIMESTAMPDIFF(SECOND, s.submitted_at, s.graded_at)), 0.0) " +
                   "FROM submissions s " +
                   "WHERE s.graded_at IS NOT NULL AND s.status NOT IN ('GRADING', 'ERROR')",
           nativeQuery = true)
    Double calculateAvgGradingTime();

    @Query("SELECT COALESCE(SUM(s.totalUserTokens), 0L) FROM Submission s " +
           "WHERE s.submittedAt >= :oneMonthAgo AND s.totalUserTokens IS NOT NULL")
    Long calculateMonthTokenUsage(@Param("oneMonthAgo") LocalDateTime oneMonthAgo);

    @Query("SELECT p.id, p.title, p.evaluationType, COUNT(s), COALESCE(AVG(s.score), 0.0), "
            + "SUM(CASE WHEN s.status = 'PASSED' THEN 1L ELSE 0L END), "
            + "SUM(CASE WHEN s.status = 'FAILED' THEN 1L ELSE 0L END), "
            + "SUM(CASE WHEN s.status = 'ERROR' THEN 1L ELSE 0L END) "
            + "FROM Problem p LEFT JOIN Submission s ON s.problem = p "
            + "GROUP BY p.id, p.title, p.evaluationType")
    List<Object[]> findProblemTableStatsRaw();

    // 2번 수정: efficiencyList는 별도 쿼리로 분리 유지 (6, 7번 데드코드 제거)
    @Query("SELECT p.id, p.title, p.evaluationType, AVG(s.totalUserTokens) "
            + "FROM Problem p JOIN Submission s ON s.problem = p "
            + "WHERE s.totalUserTokens IS NOT NULL "
            + "AND s.totalUserTokens > 0 "
            + "GROUP BY p.id, p.title, p.evaluationType")
    List<Object[]> findAllTokenStatsRaw();

    String REQUIREMENT_STATS_QUERY =
            "SELECT r.problem_id, p.title, r.description, r.weight, " +
            "       COALESCE(AVG(jt.score), 0.0) as avg_achieve, " +
            "       COALESCE((SUM(CASE WHEN jt.score < 50 THEN 1 ELSE 0 END) * 100.0 / COUNT(DISTINCT sub.id)), 0.0) as fail_rate " +
            "FROM requirements r " +
            "JOIN problems p ON p.id = r.problem_id " +
            "JOIN submissions sub ON sub.problem_id = r.problem_id " +
            "JOIN JSON_TABLE(sub.requirements_result, '$[*]' COLUMNS( " +
            "    req_id BIGINT PATH '$.id', " +
            "    score INT PATH '$.score' " +
            ")) AS jt " +
            "WHERE jt.req_id = r.id " + // <--- 요구사항 고유 ID 매핑 조건 추가로 카테시안 곱 완벽 차단!
            "GROUP BY r.id, r.problem_id, p.title, r.description, r.weight";

    @Query(value = REQUIREMENT_STATS_QUERY, nativeQuery = true)
    List<Object[]> findRequirementStatsRaw();

    @Query("SELECT p.id, s.user.nickname, p.title, s.score, s.status, s.submittedAt "
            + "FROM Submission s "
            + "JOIN s.problem p "
            + "WHERE s.status != 'GRADING' "
            + "ORDER BY s.submittedAt DESC")
    List<Object[]> findRecentSubmissions(Pageable pageable);

    @Query(value =
            "SELECT TOP_SUB.problem_id, TOP_SUB.title, TOP_SUB.nickname, TOP_SUB.score, TOP_SUB.total_user_tokens, TOP_SUB.submitted_at " +
            "FROM (" +
            "    SELECT p.id AS problem_id, p.title, u.nickname, s.score, s.total_user_tokens, s.submitted_at, " +
            "           ROW_NUMBER() OVER (" +
            "               PARTITION BY s.problem_id " +
            "               ORDER BY s.score DESC, s.total_user_tokens ASC, s.submitted_at ASC" +
            "           ) as rn " +
            "    FROM submissions s " +
            "    JOIN problems p ON p.id = s.problem_id " +
            "    JOIN users u ON u.id = s.user_id " +
            "    WHERE s.status = 'PASSED'" +
            ") TOP_SUB " +
            "WHERE TOP_SUB.rn = 1 " +
            "ORDER BY TOP_SUB.score DESC, TOP_SUB.total_user_tokens ASC",
            nativeQuery = true)
    List<Object[]> findTopScorePerProblem();
}