package com.howprom.repository;

import com.howprom.common.entity.Submission;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AdminDashboardRepository extends JpaRepository<Submission, Long> {

    @Query("SELECT COUNT(s) FROM Submission s")
    Long countTotalSubmissions();

    @Query("SELECT COALESCE((SUM(CASE WHEN s.status = 'PASSED' THEN 1.0 ELSE 0.0 END) / COUNT(s)) * 100, 0.0) FROM Submission s")
    Double calculateTotalPassRate();

    @Query("SELECT COUNT(DISTINCT s.user.id) FROM Submission s")
    Long countActiveUsers();

    @Query("SELECT COUNT(p) FROM Problem p WHERE p.isPublic = true")
    Long countPublicProblems();

    @Query("SELECT COUNT(p) FROM Problem p")
    Long countTotalProblems();

    // 2번: evaluationType 포함
    @Query("SELECT p.id, p.title, p.evaluationType, COUNT(s), COALESCE(AVG(s.score), 0.0), "
            + "SUM(CASE WHEN s.status = 'PASSED' THEN 1L ELSE 0L END), "
            + "SUM(CASE WHEN s.status = 'FAILED' THEN 1L ELSE 0L END), "
            + "SUM(CASE WHEN s.status = 'ERROR' THEN 1L ELSE 0L END) "
            + "FROM Problem p LEFT JOIN Submission s ON s.problem = p "
            + "GROUP BY p.id, p.title, p.evaluationType")
    List<Object[]> findProblemTableStatsRaw();

    // 5번 수정: EFFICIENCY 타입만 필터링
    @Query("SELECT p.id, p.title, AVG(s.totalUserTokens) "
            + "FROM Problem p JOIN Submission s ON s.problem = p "
            + "WHERE p.evaluationType = 'EFFICIENCY' "
            + "AND s.totalUserTokens IS NOT NULL "
            + "GROUP BY p.id, p.title")
    List<Object[]> findEfficiencyTokenStats();

    @Query("SELECT p.id, p.title, AVG(s.totalUserTokens), p.evaluationType "
            + "FROM Problem p JOIN Submission s ON s.problem = p "
            + "WHERE s.totalUserTokens IS NOT NULL "
            + "GROUP BY p.id, p.title, p.evaluationType")
    List<Object[]> findTokenStatsRaw();
    
    String REQUIREMENT_STATS_QUERY =
            "SELECT r.problem_id, p.title, r.description, r.weight, " +
            "AVG(jt.score) as avg_achieve, " +
            "(COUNT(CASE WHEN jt.score < 50 THEN 1 END) * 100.0 / COUNT(*)) as fail_rate " +
            "FROM requirements r " +
            "JOIN problems p ON p.id = r.problem_id " +
            "JOIN submissions sub ON sub.problem_id = r.problem_id " +
            "JOIN JSON_TABLE(sub.requirements_result, '$[*]' COLUMNS( " +
            "    req_id BIGINT PATH '$.id', " +
            "    score INT PATH '$.score' " +
            ")) AS jt ON jt.req_id = r.id " +
            "GROUP BY r.problem_id, p.title, r.description, r.weight";

    @Query(value = REQUIREMENT_STATS_QUERY, nativeQuery = true)
    List<Object[]> findRequirementStatsRaw();

    // 1번 수정: Pageable로 DB 레벨에서 10건 제한
    @Query("SELECT s.user.nickname, p.title, s.score, s.status, s.submittedAt "
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
            "               PARTITION BY s.problem_id " + // 💡 문제별로 그룹을 묶어서
            "               ORDER BY s.score DESC, s.total_user_tokens ASC, s.submitted_at ASC" + // 💡 1등 기준 정의
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