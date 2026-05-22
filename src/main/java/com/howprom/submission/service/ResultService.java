package com.howprom.submission.service;

import com.howprom.repository.RequirementRepository;
import com.howprom.common.entity.Requirement;
import com.howprom.common.entity.Submission;
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

        return new ResultViewDto(
                s.getProblem().getId(),
                s.getProblem().getTitle(),
                s.getProblem().getEvaluationType().name(),
                s.getScore(),
                s.getStatus().name(),
                s.getTotalUserTokens(),
                turnCount
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
                    return new RequirementResultViewDto(req.getDescription(), r.getScore(), req.getWeight(), pct);
                })
                .toList();
    }
}
