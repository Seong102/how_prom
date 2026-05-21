package com.howprom.submission.repository;

import com.howprom.common.entity.Submission;
import com.howprom.submission.dto.MyPageListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    long countByUser_IdAndStatus(Long userId, String status);

    /**
     * 4. 유저가 '통과(PASSED)'한 서로 다른 문제의 개수 계산 (중복 문제 제거) [cite: 105, 177]
     */
    @Query("SELECT COUNT(DISTINCT s.problem.id) FROM Submission s WHERE s.user.id = :userId AND s.status = :status")
    long countDistinctProblemIdByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
}