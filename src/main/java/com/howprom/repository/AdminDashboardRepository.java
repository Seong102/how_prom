package com.howprom.repository;

import com.howprom.common.entity.Submission;
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

    @Query("SELECT p.id, p.title, p.evaluationType, COUNT(s), COALESCE(AVG(s.score), 0.0), "
            + "SUM(CASE WHEN s.status = 'PASSED' THEN 1L ELSE 0L END), "
            + "SUM(CASE WHEN s.status = 'FAILED' THEN 1L ELSE 0L END), "
            + "SUM(CASE WHEN s.status = 'ERROR' THEN 1L ELSE 0L END) "
            + "FROM Problem p LEFT JOIN Submission s ON s.problem = p "
            + "GROUP BY p.id, p.title, p.evaluationType")
    List<Object[]> findProblemTableStatsRaw();

    @Query("SELECT p.id, p.title, AVG(s.totalUserTokens) "
            + "FROM Problem p JOIN Submission s ON s.problem = p "
            + "WHERE s.totalUserTokens IS NOT NULL "
            + "GROUP BY p.id, p.title")
    List<Object[]> findEfficiencyTokenStats();

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

    //최근 제출 현황 10건
    @Query("SELECT s.user.nickname, p.title, s.score, s.status, s.submittedAt "
            + "FROM Submission s "
            + "JOIN s.problem p "
            + "WHERE s.status != 'GRADING' "
            + "ORDER BY s.submittedAt DESC")
    List<Object[]> findRecentSubmissions();

    //문제별 최고 점수 랭킹
    @Query("SELECT p.id, p.title, s.user.nickname, s.score, s.totalUserTokens, s.submittedAt "
            + "FROM Submission s "
            + "JOIN s.problem p "
            + "WHERE s.status = 'PASSED' "
            + "AND s.score = ("
            + "    SELECT MAX(s2.score) FROM Submission s2 "
            + "    WHERE s2.problem = s.problem AND s2.status = 'PASSED'"
            + ") "
            + "ORDER BY s.score DESC, s.totalUserTokens ASC")
    List<Object[]> findTopScorePerProblem();
}