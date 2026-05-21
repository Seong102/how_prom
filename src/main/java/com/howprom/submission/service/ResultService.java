package com.howprom.submission.service;

import com.howprom.admin.repository.RequirementRepository;
import com.howprom.common.entity.Requirement;
import com.howprom.common.entity.Submission;
import com.howprom.submission.dto.RequirementResultViewDto;
import com.howprom.submission.dto.ResultViewDto;
import com.howprom.submission.repository.SubmissionRepository;
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

        int turnCount = (int) s.getConversation().stream()
                .filter(m -> "user".equals(m.getRole()))
                .count();

        return new ResultViewDto(
                s.getProblem().getTitle(),
                s.getProblem().getEvaluationType(),
                s.getScore(),
                s.getStatus(),
                s.getTotalUserTokens(),
                turnCount
        );
    }

    public List<RequirementResultViewDto> getRequirementResults(Long submissionId) {
        Submission s = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found: " + submissionId));

        return s.getRequirementsResult().stream()
                .map(r -> {
                    Requirement req = requirementRepository.findById(r.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Requirement not found: " + r.getId()));
                    int pct = req.getWeight() > 0 ? (r.getScore() * 100 / req.getWeight()) : 0;
                    return new RequirementResultViewDto(req.getContent(), r.getScore(), req.getWeight(), pct);
                })
                .toList();
    }
}
