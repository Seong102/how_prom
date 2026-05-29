package com.howprom.admin.service;

import com.howprom.common.entity.EvaluationType;
import com.howprom.common.entity.Problem;
import com.howprom.common.entity.Requirement;
import com.howprom.common.entity.User;
import com.howprom.admin.dto.ProblemAdminDTO;
import com.howprom.admin.dto.ProblemStatsDTO;
import com.howprom.repository.ProblemRepository;
import com.howprom.repository.SubmissionRepository;
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
    private final SubmissionRepository submissionRepository; // 팩토리 및 제출 이력 체크용 추가

    /**
     * 전체 문제 목록 조회
     */
    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }

    /**
     * 안전한 DTO 단건 조회 (JOIN FETCH 적용으로 LazyInitializationException 방지)
     */
    public ProblemAdminDTO getProblemDetailAsDTO(Long id) {
        Problem problem = problemRepository.findByIdWithRequirements(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 문제가 존재하지 않습니다. id=" + id));
        return ProblemAdminDTO.from(problem);
    }

    /**
     * 컨트롤러/필터 검색용 DTO 리스트 조회
     */
    public List<ProblemAdminDTO> getProblemsAsDTO(String keyword) {
        List<Problem> problems = this.getProblems(keyword);
        return problems.stream()
                .map(ProblemAdminDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * 새 문제 등록
     */
    @Transactional
    public void createProblem(ProblemAdminDTO dto, List<String> reqDescs, List<Integer> reqWeights, User creator) {

        if (reqDescs != null && reqWeights != null) {
            if (reqDescs.size() != reqWeights.size()) {
                throw new IllegalArgumentException("요구사항 설명과 배점의 개수가 일치하지 않습니다.");
            }
            int totalWeight = reqWeights.stream().mapToInt(Integer::intValue).sum();
            if (totalWeight != 100) {
                throw new IllegalArgumentException("요구사항 배점의 총합은 100점이어야 합니다. (현재: " + totalWeight + "점)");
            }
        } else if (reqDescs != null || reqWeights != null) {
            throw new IllegalArgumentException("요구사항 명세와 배점은 동시에 존재해야 합니다.");
        }

        EvaluationType evalType = EvaluationType.valueOf(dto.getEvaluationType());

        float correctness = 1.0f;
        float efficiency = 0.0f;

        if (EvaluationType.EFFICIENCY.equals(evalType)) {
            correctness = 0.7f;
            efficiency = 0.3f;
        }

        // isPublic Boolean 빌더 이슈 방어 및 객체 생성
        Problem problem = Problem.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .evaluationType(evalType)
                .exampleInput(dto.getExampleInput())
                .exampleOutput(dto.getExampleOutput())
                .tokenLimit(dto.getTokenLimit())
                .correctnessWeight(correctness)
                .efficiencyWeight(efficiency)
                .avgUserTokens(0.0f)
                .createdBy(creator)
                .build();

        // 롬복 primitive/wrapper 필드명 충돌 방어용 setter 처리
        problem.setIsPublic(dto.getIsPublic() != null ? dto.getIsPublic() : false);
        problem.setRequirements(new ArrayList<>());

        if (reqDescs != null && reqWeights != null) {
            for (int i = 0; i < reqDescs.size(); i++) {
                Requirement requirement = Requirement.builder()
                        .description(reqDescs.get(i))
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

        if (reqDescs != null && reqWeights != null) {
            if (reqDescs.size() != reqWeights.size()) {
                throw new IllegalArgumentException("요구사항 설명과 배점의 개수가 일치하지 않습니다.");
            }
            int totalWeight = reqWeights.stream().mapToInt(Integer::intValue).sum();
            if (totalWeight != 100) {
                throw new IllegalArgumentException("요구사항 배점의 총합은 100점이어야 합니다. (현재: " + totalWeight + "점)");
            }
        } else if (reqDescs != null || reqWeights != null) {
            throw new IllegalArgumentException("요구사항 명세와 배점은 동시에 존재해야 합니다.");
        }

        EvaluationType evalType = EvaluationType.valueOf(dto.getEvaluationType());

        problem.setTitle(dto.getTitle());
        problem.setDescription(dto.getDescription());
        problem.setEvaluationType(evalType);
        problem.setExampleInput(dto.getExampleInput());
        problem.setExampleOutput(dto.getExampleOutput());
        problem.setIsPublic(dto.getIsPublic() != null ? dto.getIsPublic() : false);
        problem.setTokenLimit(dto.getTokenLimit());

        if (EvaluationType.EFFICIENCY.equals(evalType)) {
            problem.setCorrectnessWeight(0.7f);
            problem.setEfficiencyWeight(0.3f);
        } else {
            problem.setCorrectnessWeight(1.0f);
            problem.setEfficiencyWeight(0.0f);
        }

        problem.getRequirements().clear();

        if (reqDescs != null && reqWeights != null) {
            for (int i = 0; i < reqDescs.size(); i++) {
                Requirement requirement = Requirement.builder()
                        .description(reqDescs.get(i))
                        .weight(reqWeights.get(i))
                        .displayOrder(i + 1)
                        .problem(problem)
                        .build();
                problem.getRequirements().add(requirement);
            }
        }
    }

    /**
     * 문제 영구 삭제 (비공개 상태 & 제출 이력이 없는 경우만 가능)
     */
    @Transactional
    public void deleteProblem(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 문제가 존재하지 않습니다. id=" + id));
        
        // 1. 공개 상태인 문제는 삭제 거부
        if (Boolean.TRUE.equals(problem.getIsPublic())) {
            throw new IllegalStateException("공개 상태인 문항은 삭제할 수 없습니다. 먼저 비공개로 변경해 주세요.");
        }
        
        // 2. 제출 이력이 있는 문제는 무결성을 위해 삭제 거부
        long submissionCount = submissionRepository.countByProblemId(id);
        if (submissionCount > 0) {
            throw new IllegalStateException("제출 이력이 있는 문제는 삭제할 수 없습니다. (현재 " + submissionCount + "건)");
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
     * 문제 엔티티 단건 조회
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
            Long pid = Long.parseLong(keyword);
            return problemRepository.findById(pid)
                    .map(List::of)
                    .orElse(List.of());
        }
        return problemRepository.findByTitleContaining(keyword);
    }

    /**
     * 문제 통계 데이터 맵 반환 (메인 테이블 렌더링용)
     */
    public Map<Long, ProblemStatsDTO> getStatsMap() {
        return problemRepository.getProblemStatistics().stream()
                .collect(Collectors.toMap(ProblemStatsDTO::getProblemId, stats -> stats));
    }
}