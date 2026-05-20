package com.howprom.admin.service;

import com.howprom.common.entity.Problem; // 공통 엔티티 참조로 수정
import com.howprom.admin.dto.ProblemStatsDTO;
import com.howprom.admin.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminProblemService {

    private final ProblemRepository problemRepository;

    /**
     * 전체 문제 목록 조회
     */
    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }

    /**
     * 문제 영구 삭제 (비공개 상태만 가능)
     */
    @Transactional
    public void deleteProblem(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 문제가 존재하지 않습니다. id=" + id));
        if (problem.getIsPublic()) {
            throw new IllegalStateException("공개 상태인 문항은 삭제할 수 없습니다. 먼저 비공개로 변경해 주세요.");
        }
        problemRepository.deleteById(id);
    }

    /**
     * 문제 공개 여부(isPublic) 상태 토글 변경
     */
    @Transactional
    public void updatePublicStatus(Long id, boolean isPublic) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 문제가 존재하지 않습니다. id=" + id));
        problem.setIsPublic(isPublic); 
    }
    
    /**
     * 문제 단건 조회 (수정 화면 진입용)
     */
    public Problem findById(Long id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 문제가 존재하지 않습니다. id=" + id));
    }
    
    /**
     * 문제 검색: 번호(숫자) 또는 제목(문자열) 통합 조회
     */
    public List<Problem> getProblems(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return problemRepository.findAll();
        }
        // 1. 키워드가 숫자인지 확인
        if (keyword.matches("\\d+")) {
            Long id = Long.parseLong(keyword);
            return problemRepository.findById(id)
                    .map(List::of)
                    .orElse(List.of());
        }
        // 2. 숫자가 아니면 제목으로 검색
        return problemRepository.findByTitleContaining(keyword);
    }
    
    public Map<Long, ProblemStatsDTO> getStatsMap() {
        return problemRepository.getProblemStatistics().stream()
                .collect(Collectors.toMap(ProblemStatsDTO::getProblemId, stats -> stats));
    }
}