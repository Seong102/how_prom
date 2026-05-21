package com.howprom.problem.service;
 
import com.howprom.common.entity.Problem;
import com.howprom.common.entity.Requirement;
import com.howprom.problem.dto.ProblemDetailDto;
import com.howprom.repository.ProblemRepository;
import com.howprom.repository.RequirementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.util.List;
 
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemDetailService {
 
    private final ProblemRepository problemRepository;
    private final RequirementRepository requirementRepository;
 
    /**
     * SCR-PROB-02 문제 상세 조회
     * - 공개된 문제만 조회 (비공개 문제 직접 URL 접근 차단)
     * - 요구사항 목록 display_order 오름차순 정렬
     */
    public ProblemDetailDto getProblemDetail(Long problemId) {
        // 1. 문제 조회 - 공개된 문제만, 없으면 예외
        Problem problem = problemRepository.findByIdAndIsPublicTrue(problemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 비공개 문제입니다. id=" + problemId));
 
        // 2. 요구사항 목록 조회 - display_order 오름차순
        List<Requirement> requirements = requirementRepository.findByProblemIdOrderByDisplayOrderAsc(problemId);
 
        // 3. DTO 조립 후 반환
        return ProblemDetailDto.of(problem, requirements);
    }
}