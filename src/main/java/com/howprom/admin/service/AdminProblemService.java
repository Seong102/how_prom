package com.howprom.admin.service;

import com.howprom.admin.entity.Problem;
import com.howprom.admin.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

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
     * 문제 검색: 번호(숫자) 또는 제목(문자열) 통합 조회
     */
    public List<Problem> getProblems(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return problemRepository.findAll();
        }
        // 1. 키워드가 숫자인지 확인
        if (keyword.matches("\\d+")) {
            Long id = Long.parseLong(keyword);
            // 해당 ID가 존재하면 리스트에 담아 반환, 없으면 빈 리스트 반환
            return problemRepository.findById(id)
                    .map(List::of)
                    .orElse(List.of());
        }
        // 2. 숫자가 아니면 제목으로 검색
        return problemRepository.findByTitleContaining(keyword);
    }
}