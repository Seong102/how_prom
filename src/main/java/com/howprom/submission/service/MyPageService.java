package com.howprom.submission.service;

import com.howprom.submission.dto.*;
import com.howprom.common.entity.Submission;
import com.howprom.common.entity.SubmissionStatus;
import com.howprom.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final SubmissionRepository submissionRepository;

    /**
     * 1. 마이페이지 메인 데이터 조회 (SCR-MY-01)
     * 대용량 JSON(conversation) 컬럼을 제외한 경량 데이터 페이징 및 상단 통계 가공
     */
    public MyPageResponseDto getMyPageData(Long userId, Pageable pageable) {
        // 하단 테이블용 페이징 목록 조회 (Repository에서 DTO로 직접 프로젝션 조회하여 디스크 I/O 최적화)
        Page<MyPageListDto> paginationSubmissions = submissionRepository.findMyPageListByUserId(userId, pageable);

        // 상단 대시보드 통계 계산용 원천 데이터 집계
        long totalSubmissions = submissionRepository.countByUser_Id(userId);
        long totalSolvedProblems = submissionRepository.countDistinctProblemIdByUserIdAndStatus(userId, SubmissionStatus.PASSED);
        long passedCount = submissionRepository.countByUser_IdAndStatus(userId, SubmissionStatus.PASSED);

        // 방어 코딩: 가입 후 제출 이력이 없는 신규 유저의 분모 0(ArithmeticException) 예외 방지
        double passRate = 0.0;
        if (totalSubmissions > 0) {
            passRate = Math.round(((double) passedCount / totalSubmissions * 100) * 10) / 10.0;
        }

        // 통계 DTO 결합
        MyPageStatsDto statsDto = MyPageStatsDto.builder()
                .totalSubmissions(totalSubmissions)
                .totalSolvedProblems(totalSolvedProblems)
                .passRate(passRate)
                .build();

        // 메인 화면 통합 응답 DTO 반환
        return MyPageResponseDto.builder()
                .stats(statsDto)
                .submissions(paginationSubmissions)
                .build();
    }

    /**
     * 2. 마이페이지 기록 클릭 시 상세 내역 조회
     * 특정 제출의 대화 히스토리(conversation JSON) 및 피드백 본문 반환
     */
    public MyPageDetailResponseDto getSubmissionDetail(Long submissionId, Long userId) {
        // 단건 상세 데이터 로드
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 제출 기록입니다."));

        // 인가(Authorization) 검증: 파라미터 변조를 통해 타인의 대화 이력을 가로채는 위험 차단
        if (!submission.getUser().getId().equals(userId)) {
            throw new SecurityException("해당 제출 기록의 상세 내용을 열람할 권한이 없습니다.");
        }

        // 상세 팝업용 응답 DTO 변환 (Hibernate 6가 컬럼의 JSON 문자열을 객체 List로 변환 완료한 상태)
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