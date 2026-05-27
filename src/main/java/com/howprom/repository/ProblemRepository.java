package com.howprom.repository;

import com.howprom.admin.dto.ProblemStatsDTO;
import com.howprom.common.entity.EvaluationType;
import com.howprom.common.entity.Problem;
import com.howprom.problem.dto.ProblemListItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    /**
     * 관리자 대시보드용 문제별 통계 조회
     */
    @Query("SELECT new com.howprom.admin.dto.ProblemStatsDTO(" +
           "  s.problem.id, " +
           "  COUNT(s.id), " +
           "  AVG(s.score), " +
           "  SUM(CASE WHEN s.status = 'PASSED' THEN 1L ELSE 0L END)" +
           ") " +
           "FROM Submission s " +
           "GROUP BY s.problem.id")
    List<ProblemStatsDTO> getProblemStatistics();

    List<Problem> findByTitleContaining(String title);

    List<Problem> findByIsPublicTrueOrderByCreatedAtAsc();

    Optional<Problem> findByIdAndIsPublicTrue(Long id);
    
    List<Problem> findByIsPublicTrueAndCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime since, Pageable pageable);
    
    /**
     * [비로그인용] 공개 문제 목록 조회 (필터 및 페이징)
     */
    @Query("""
        SELECT new com.howprom.problem.dto.ProblemListItem(
            p.id, p.title, p.evaluationType, p.createdAt, NULL, NULL
        )
        FROM Problem p
        WHERE p.isPublic = true
          AND (:evaluationType IS NULL OR p.evaluationType = :evaluationType)
        """)
    Page<ProblemListItem> findPublicListForGuest(
            @Param("evaluationType") EvaluationType evaluationType,
            Pageable pageable);

    /**
     * [로그인용] 공개 문제 목록 + 사용자별 풀이 상태 및 최고 점수 조회
     * * 정렬 방어 조치: 네이ティブ 쿼리 특성상 Pageable의 Sort 객체가 자바 필드명(createdAt)으로 
     * 바인딩되면 SQL 에러가 발생하므로, 쿼리 내부에 고정 정렬(ORDER BY p.created_at DESC)을 명시함.
     */
    @Query(value = """
        SELECT
            p.id AS id,
            p.title AS title,
            p.evaluation_type AS evaluationType,
            p.created_at AS createdAt,
            (CASE
                WHEN SUM(CASE WHEN s.status = 'PASSED' THEN 1 ELSE 0 END) > 0 THEN 'PASSED'
                ELSE (SELECT s2.status FROM submissions s2
                      WHERE s2.problem_id = p.id AND s2.user_id = :userId
                      ORDER BY s2.submitted_at DESC LIMIT 1)
            END) AS status,
            MAX(CASE WHEN s.status = 'PASSED' THEN s.score END) AS bestScore
        FROM problems p
        LEFT JOIN submissions s
               ON s.problem_id = p.id AND s.user_id = :userId
        WHERE p.is_public = true
          AND (:evaluationType IS NULL OR p.evaluation_type = :evaluationType)
        GROUP BY p.id, p.title, p.evaluation_type, p.created_at
        HAVING (
            :statusFilter = 'ALL'
            OR (:statusFilter = 'PASSED_ONLY' AND SUM(CASE WHEN s.status = 'PASSED' THEN 1 ELSE 0 END) > 0)
            OR (:statusFilter = 'NOT_SOLVED' AND SUM(CASE WHEN s.status = 'PASSED' THEN 1 ELSE 0 END) = 0)
        )
        ORDER BY p.created_at DESC
        """,
        countQuery = """
        SELECT COUNT(*) FROM (
            SELECT p.id
            FROM problems p
            LEFT JOIN submissions s
                   ON s.problem_id = p.id AND s.user_id = :userId
            WHERE p.is_public = true
              AND (:evaluationType IS NULL OR p.evaluation_type = :evaluationType)
            GROUP BY p.id
            HAVING (
                :statusFilter = 'ALL'
                OR (:statusFilter = 'PASSED_ONLY' AND SUM(CASE WHEN s.status = 'PASSED' THEN 1 ELSE 0 END) > 0)
                OR (:statusFilter = 'NOT_SOLVED' AND SUM(CASE WHEN s.status = 'PASSED' THEN 1 ELSE 0 END) = 0)
            )
        ) AS subq
        """,
        nativeQuery = true)
    Page<Object[]> findPublicListForUser(
            @Param("userId") Long userId,
            @Param("evaluationType") String evaluationType,
            @Param("statusFilter") String statusFilter,
            Pageable pageable);
    
    /**
     * [대시보드 추천] 평가 유형별 해결하지 않은 최신 문제 조회
     */
    @Query("""
        SELECT p FROM Problem p
        WHERE p.isPublic = true
          AND p.evaluationType = :evaluationType
          AND p.id NOT IN (
              SELECT s.problem.id FROM Submission s
              WHERE s.user.id = :userId
                AND s.status = com.howprom.common.entity.SubmissionStatus.PASSED
          )
        ORDER BY p.createdAt DESC
        """)
    List<Problem> findRecommendedByType(
            @Param("evaluationType") EvaluationType evaluationType,
            @Param("userId") Long userId,
            Pageable pageable);
    
    @Query("SELECT p FROM Problem p JOIN FETCH p.requirements WHERE p.id = :id")
    Optional<Problem> findByIdWithRequirements(@Param("id") Long id);
}