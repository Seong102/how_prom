package com.howprom.repository;

import com.howprom.main.dto.TopSubmissionRow;
import com.howprom.main.dto.ContinueProblemRow;
import com.howprom.common.entity.Submission;
import com.howprom.common.entity.SubmissionStatus;
import com.howprom.community.dto.CommunityListDto;
import com.howprom.submission.dto.MyPageListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    /**
     * 1. 마이페이지 목록 조회 (성능 최적화 경량 쿼리)
     * s.conversation JSON 컬럼을 완전히 제외하고, DTO 생성자로 필요한 필드만 즉시 프로젝션하여 조회합니다[cite: 177, 179].
     */
    @Query(value = "SELECT new com.howprom.submission.dto.MyPageListDto(" +
            "s.id, p.id, p.title, p.evaluationType, s.score, s.status, s.totalUserTokens, s.submittedAt) " +
            "FROM Submission s " +
            "JOIN s.problem p " +
            "WHERE s.user.id = :userId",
            countQuery = "SELECT COUNT(s) FROM Submission s WHERE s.user.id = :userId")
    Page<MyPageListDto> findMyPageListByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 2. 유저별 총 제출 횟수 계산 [cite: 105, 177]
     */
    long countByUser_Id(Long userId);

    /**
     * 3. 유저별 특정 상태(예: 'PASSED')의 제출 횟수 계산 [cite: 105, 177]
     */
    long countByUser_IdAndStatus(Long userId, SubmissionStatus status);

    /**
     * 4. 유저가 '통과(PASSED)'한 서로 다른 문제의 개수 계산 (중복 문제 제거) [cite: 105, 177]
     */
    @Query("SELECT COUNT(DISTINCT s.problem.id) FROM Submission s WHERE s.user.id = :userId AND s.status = :status")
    long countDistinctProblemIdByUserIdAndStatus(@Param("userId") Long userId, @Param("status") SubmissionStatus status);

    /**
     * 5. 커뮤니티 — 같은 문제의 PASSED 제출 목록 (본인 제외, 점수 내림차순)
     */
    @Query("SELECT new com.howprom.community.dto.CommunityListDto(" +
            "s.id, u.nickname, s.score, s.totalUserTokens, s.submittedAt) " +
            "FROM Submission s JOIN s.user u " +
            "WHERE s.problem.id = :problemId AND s.status = :status " +
            "ORDER BY s.score DESC, s.totalUserTokens ASC")
    List<CommunityListDto> findCommunityList(@Param("problemId") Long problemId,
                                             @Param("status") SubmissionStatus status,
                                             @Param("excludeUserId") Long excludeUserId);
    
    
    /**
     * 대시보드 — 이번 주 우수 풀이 (PASSED 제출 중 점수 상위)
     *
     * @param weekStart   이번 주 시작 (월요일 00:00)
     * @param weekEnd     이번 주 끝 (다음 주 월요일 00:00)
     * @param viewerId    조회자 ID — 본인이 그 문제를 PASSED 받은 적 있는지 판정용 (비로그인 시 -1)
     * @param pageable    상위 N개 제한용
     */
    @Query("""
        SELECT new com.howprom.main.dto.TopSubmissionRow(
            s.id,
            s.problem.id,
            s.problem.title,
            u.nickname,
            s.score,
            (CASE WHEN EXISTS (
                SELECT 1 FROM Submission s2
                WHERE s2.problem.id = s.problem.id
                  AND s2.user.id = :viewerId
                  AND s2.status = com.howprom.common.entity.SubmissionStatus.PASSED
            ) THEN true ELSE false END)
        )
        FROM Submission s
        JOIN s.user u
        WHERE s.status = com.howprom.common.entity.SubmissionStatus.PASSED
          AND s.submittedAt >= :weekStart
          AND s.submittedAt <  :weekEnd
        ORDER BY s.score DESC, s.totalUserTokens ASC, s.submittedAt ASC
        """)
    List<TopSubmissionRow> findTopWeeklySubmissions(
            @Param("weekStart") LocalDateTime weekStart,
            @Param("weekEnd") LocalDateTime weekEnd,
            @Param("viewerId") Long viewerId,
            Pageable pageable);
    
    /**
     * 대시보드 — 이어서 풀기
     * "아직 PASSED 받지 못한 문제 중 가장 최근에 시도한 것"
     *
     * 1. 사용자의 PASSED 받은 problem_id 목록 제외
     * 2. 남은 제출 중 submitted_at 가장 최근 1건
     * 3. 그 제출의 status가 진짜 "재도전 필요" 상태 (FAILED/GRADING/ERROR)
     */
    @Query("""
        SELECT new com.howprom.main.dto.ContinueProblemRow(
            s.problem.id,
            s.problem.title,
            s.problem.evaluationType,
            s.status,
            s.score,
            s.submittedAt
        )
        FROM Submission s
        WHERE s.user.id = :userId
          AND s.problem.id NOT IN (
              SELECT s2.problem.id FROM Submission s2
              WHERE s2.user.id = :userId
                AND s2.status = com.howprom.common.entity.SubmissionStatus.PASSED
          )
        ORDER BY s.submittedAt DESC
        """)
    List<ContinueProblemRow> findContinueProblem(
            @Param("userId") Long userId,
            Pageable pageable);
}