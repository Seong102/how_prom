package com.howprom.problem.repository;

import com.howprom.common.entity.Requirement;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
 
public interface RequirementRepository extends JpaRepository<Requirement, Long> {
 
    // 문제별 요구사항 목록 조회 - 화면 표시 순서대로 (SCR-PROB-02 요구사항 목록)
    List<Requirement> findByProblemIdOrderByDisplayOrderAsc(Long problemId);
}
 