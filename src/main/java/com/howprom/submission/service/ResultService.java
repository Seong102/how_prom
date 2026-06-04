package com.howprom.submission.service;

import com.howprom.repository.RequirementRepository;
import com.howprom.common.entity.EvaluationType;
import com.howprom.common.entity.Problem;
import com.howprom.common.entity.Requirement;
import com.howprom.common.entity.Submission;
import com.howprom.submission.dto.RequirementResultDto;
import com.howprom.submission.dto.RequirementResultViewDto;
import com.howprom.submission.dto.ResultViewDto;
import com.howprom.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResultService {

    private final SubmissionRepository submissionRepository;
    private final RequirementRepository requirementRepository;

    public ResultViewDto getResultView(Long submissionId) {
        Submission s = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found: " + submissionId));

        int turnCount = s.getConversation() == null ? 0 :
                (int) s.getConversation().stream()
                        .filter(m -> "user".equals(m.getRole()))
                        .count();

        Problem problem = s.getProblem();

        // EFFICIENCY 점수 분해 계산
        // llmScore = Σ requirementsResult[].score (LLM 원점수, requirements_result 합산)
        // correctnessPart = llmScore × correctnessWeight  (정확도 기여분)
        // efficiencyPart  = finalScore - correctnessPart   (효율성 기여분)
        int     llmScore        = 0;
        double  correctnessPart = 0.0;
        double  efficiencyPart  = 0.0;

        if (EvaluationType.EFFICIENCY.equals(problem.getEvaluationType())
                && s.getRequirementsResult() != null) {
            llmScore = s.getRequirementsResult().stream()
                    .mapToInt(RequirementResultDto::getScore)
                    .sum();
            correctnessPart = llmScore * problem.getCorrectnessWeight();
            efficiencyPart  = s.getScore() - correctnessPart;
        }

        return new ResultViewDto(
                problem.getId(),
                problem.getTitle(),
                problem.getEvaluationType().name(),
                s.getScore(),
                s.getStatus().name(),
                s.getTotalUserTokens(),
                turnCount,
                llmScore,
                correctnessPart,
                efficiencyPart,
                problem.getAvgUserTokens()
        );
    }

    public List<RequirementResultViewDto> getRequirementResults(Long submissionId) {
        Submission s = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found: " + submissionId));

        if (s.getRequirementsResult() == null) {
            return List.of();
        }

        return s.getRequirementsResult().stream()
                .map(r -> {
                    Requirement req = requirementRepository.findById(r.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Requirement not found: " + r.getId()));
                    int pct = req.getWeight() > 0 ? (r.getScore() * 100 / req.getWeight()) : 0;
                    return new RequirementResultViewDto(req.getDescription(), r.getScore(), req.getWeight(), pct, r.getComment());
                })
                .toList();
    }
}