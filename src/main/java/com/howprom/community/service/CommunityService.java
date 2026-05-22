package com.howprom.community.service;

import com.howprom.common.entity.Submission;
import com.howprom.common.entity.Submission.SubmissionStatus;
import com.howprom.community.dto.CommunityListDto;
import com.howprom.repository.SubmissionRepository;
import com.howprom.submission.dto.MyPageDetailResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final SubmissionRepository submissionRepository;

    /**
     * 특정 문제의 PASSED 제출 목록 (본인 제외, 점수 내림차순)
     */
    public List<CommunityListDto> getCommunityList(Long problemId, Long excludeUserId) {
        return submissionRepository.findCommunityList(problemId, SubmissionStatus.PASSED, excludeUserId);
    }

    /**
     * 다른 유저의 제출 상세 조회 (소유권 검사 없이 공개 열람)
     */
    public MyPageDetailResponseDto getPublicSubmissionDetail(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 제출 기록입니다."));

        return MyPageDetailResponseDto.builder()
                .submissionId(submission.getId())
                .problemTitle(submission.getProblem().getTitle())
                .score(submission.getScore())
                .finalCode(submission.getFinalCode())
                .totalUserTokens(submission.getTotalUserTokens())
                .conversation(submission.getConversation())
                .requirementsResult(submission.getRequirementsResult())
                .build();
    }
}
