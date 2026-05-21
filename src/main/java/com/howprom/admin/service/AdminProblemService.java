package com.howprom.admin.service;

import com.howprom.common.entity.Problem;
import com.howprom.common.entity.Requirement;
import com.howprom.admin.dto.ProblemAdminDTO;
import com.howprom.admin.dto.ProblemStatsDTO;
import com.howprom.admin.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
     * 새 문제 등록
     */
    @Transactional
    public void createProblem(ProblemAdminDTO dto, List<String> reqDescs, List<Integer> reqWeights) {
        
        // 💡 [추가 로직] 평가 유형에 따라 정확성/효율성 배점 비율 자동 설정
        float correctness = 1.0f;
        float efficiency = 0.0f;
        
        if ("EFFICIENCY".equals(dto.getEvaluationType())) {
            correctness = 0.7f;
            efficiency = 0.3f;
        }

        // 1. 마스터 엔티티 생성
        Problem problem = Problem.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .evaluationType(dto.getEvaluationType())
                .exampleInput(dto.getExampleInput())
                .exampleOutput(dto.getExampleOutput())
                // 💡 [변경] 하드코딩을 지우고 DTO에서 넘어온 값을 매핑합니다.
                .difficulty(dto.getDifficulty())
                .isPublic(dto.getIsPublic())
                .tokenLimit(dto.getTokenLimit()) 
                // 평가 유형별 비율 연동
                .correctnessWeight(correctness)
                .efficiencyWeight(efficiency)
                .avgPromptTokens(0.0f)
                .build();

        // Lombok 빌더 직후 연관관계 컬렉션 초기화
        problem.setRequirements(new ArrayList<>());

        // 2. 자식 요구사항 생성 및 양방향 연관관계 매핑
        if (reqDescs != null && reqWeights != null) {
            for (int i = 0; i < reqDescs.size(); i++) {
                Requirement requirement = Requirement.builder()
                        .description(reqDescs.get(i)) // 변경: content -> description
                        .weight(reqWeights.get(i))
                        .displayOrder(i + 1)
                        .problem(problem)
                        .build();
                problem.getRequirements().add(requirement);
            }
        }

        problemRepository.save(problem);
    }

    /**
     * 기존 문제 수정
     */
    @Transactional
    public void updateProblem(Long id, ProblemAdminDTO dto, List<String> reqDescs, List<Integer> reqWeights) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 문제가 존재하지 않습니다. id=" + id));

        // 1. 마스터 정보 수정
        problem.setTitle(dto.getTitle());
        problem.setDescription(dto.getDescription());
        problem.setEvaluationType(dto.getEvaluationType());
        problem.setExampleInput(dto.getExampleInput());
        problem.setExampleOutput(dto.getExampleOutput());
        
        // 💡 [핵심 추가] 수정 페이지에서 넘어온 메타 정보 반영
        problem.setDifficulty(dto.getDifficulty());
        problem.setIsPublic(dto.getIsPublic());
        problem.setTokenLimit(dto.getTokenLimit());

        // 💡 [추가 로직] 평가 유형 변경에 따른 배점 비율 갱신
        if ("EFFICIENCY".equals(dto.getEvaluationType())) {
            problem.setCorrectnessWeight(0.7f);
            problem.setEfficiencyWeight(0.3f);
        } else {
            problem.setCorrectnessWeight(1.0f);
            problem.setEfficiencyWeight(0.0f);
        }

        // 2. 기존 연관관계 컬렉션 비우기
        problem.getRequirements().clear();

        // 3. 수정 및 추가된 요구사항 새로 조립
        if (reqDescs != null && reqWeights != null) {
            for (int i = 0; i < reqDescs.size(); i++) {
                Requirement requirement = Requirement.builder()
                        .description(reqDescs.get(i)) // 변경: content -> description
                        .weight(reqWeights.get(i))
                        .displayOrder(i + 1)
                        .problem(problem)
                        .build();
                problem.getRequirements().add(requirement);
            }
        }
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
     * 문제 공개 여부 토글 변경
     */
    @Transactional
    public void updatePublicStatus(Long id, boolean isPublic) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 문제가 존재하지 않습니다. id=" + id));
        problem.setIsPublic(isPublic); 
    }
    
    /**
     * 문제 단건 조회
     */
    public Problem findById(Long id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 문제가 존재하지 않습니다. id=" + id));
    }
    
    /**
     * 문제 검색: 번호 또는 제목 통합 조회
     */
    public List<Problem> getProblems(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return problemRepository.findAll();
        }
        if (keyword.matches("\\d+")) {
            Long id = Long.parseLong(keyword);
            return problemRepository.findById(id)
                    .map(List::of)
                    .orElse(List.of());
        }
        return problemRepository.findByTitleContaining(keyword);
    }
    
    /**
     * 문제 통계 데이터 맵 반환
     */
    public Map<Long, ProblemStatsDTO> getStatsMap() {
        return problemRepository.getProblemStatistics().stream()
                .collect(Collectors.toMap(ProblemStatsDTO::getProblemId, stats -> stats));
    }
}