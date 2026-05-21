package com.howprom.problem.repository;

import com.howprom.common.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.util.List;
import java.util.Optional;
 
public interface ProblemRepository extends JpaRepository<Problem, Long> {
 
    // 공개된 문제 목록 조회 (SCR-PROB-01 문제 목록 화면)
    List<Problem> findByIsPublicTrueOrderByCreatedAtAsc();
 
    // 문제 상세 조회 - 공개된 문제만 (SCR-PROB-02 problemDetail)
    Optional<Problem> findByIdAndIsPublicTrue(Long id);
}