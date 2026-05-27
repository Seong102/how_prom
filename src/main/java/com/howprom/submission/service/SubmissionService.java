package com.howprom.submission.service;

import com.howprom.common.entity.*;
import com.howprom.repository.ProblemRepository;
import com.howprom.repository.SubmissionRepository;
import com.howprom.submission.dto.ChatMessageDto;
import com.howprom.submission.dto.SubmitRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository    problemRepository;
    private final LlmGradingService    llmGradingService;

    @Value("${llm.system-prompt-tokens:47}")
    private int systemPromptTokens;

    /**
     * POST /api/submissions 처리
     * 1. conversation에서 finalCode 추출
     * 2. LLM #2 채점
     * 3. 점수 계산 (평가 유형별)
     * 4. submissions INSERT
     * 5. EFFICIENCY 모드면 avg_user_tokens 갱신
     * @return 저장된 submission ID (결과 화면 리다이렉트용)
     */
    @Transactional
    public Long submit(SubmitRequest request, User user) {

        // 1. 문제 조회 — requirements JOIN FETCH (Lazy Loading 방지)
        Problem problem = problemRepository.findByIdWithRequirements(request.getProblemId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문제입니다."));

        List<ChatMessageDto> conversation = request.getConversation();

        // 2. 에디터에서 직접 전송된 finalCode 사용
        String finalCode = request.getFinalCode();
        if (finalCode == null || finalCode.isBlank()) {
            throw new IllegalArgumentException("에디터에 코드를 작성한 후 제출하세요.");
        }

        // 3. BUDGET 토큰 초과 서버 재검증
        if (problem.getEvaluationType() == EvaluationType.BUDGET
                && problem.getTokenLimit() != null
                && request.getTotalUserTokens() > problem.getTokenLimit()) {
            throw new IllegalArgumentException("토큰 한도를 초과했습니다.");
        }

        // 4. LLM #2 채점 호출
        LlmGradingService.GradingResult grading = llmGradingService.grade(
                problem.getTitle(),
                problem.getDescription(),
                problem.getRequirements(),
                finalCode
        );

        // 5. 평가 유형별 최종 점수 계산
        int finalScore = calcFinalScore(problem, grading, request.getTotalUserTokens());

        // 6. 상태 결정
        SubmissionStatus status;
        if (grading.isError()) {
            status = SubmissionStatus.ERROR;
            finalScore = 0;
        } else {
            status = grading.passed() ? SubmissionStatus.PASSED : SubmissionStatus.FAILED;
        }

        // 7. Submission 저장
        Submission submission = Submission.builder()
                .user(user)
                .problem(problem)
                .conversation(conversation)
                .finalCode(finalCode)
                .totalUserTokens(request.getTotalUserTokens())
                .score(finalScore)
                .requirementsResult(grading.requirementsResult())
                .status(status)
                .build();

        submission.setGradedAt(LocalDateTime.now());
        Submission saved = submissionRepository.save(submission);

        // 8. EFFICIENCY 모드 — avg_user_tokens 실시간 갱신
        if (problem.getEvaluationType() == EvaluationType.EFFICIENCY
                && status != SubmissionStatus.ERROR) {
            updateAvgUserTokens(problem);
        }

        return saved.getId();
    }

    /** 평가 유형별 최종 점수 계산 */
    private int calcFinalScore(Problem problem, LlmGradingService.GradingResult grading, int userTokens) {
        if (grading.isError()) return 0;

        return switch (problem.getEvaluationType()) {
            case STANDARD -> grading.score();
            case EFFICIENCY -> {
                float avg = problem.getAvgUserTokens();
                if (avg <= 0) {
                    // 첫 제출자 — 효율성 계산 불가, LLM 점수 그대로
                    yield grading.score();
                }
                double efficiency  = 1.0 - ((double) userTokens / avg);
                double finalScore  = grading.score() * problem.getCorrectnessWeight()
                                   + grading.score() * efficiency * problem.getEfficiencyWeight();
                yield (int) Math.max(0, Math.min(100, Math.round(finalScore)));
            }
            case BUDGET -> grading.score();
        };
    }

    /** EFFICIENCY 모드 avg_user_tokens 갱신 */
    private void updateAvgUserTokens(Problem problem) {
        List<Submission> submissions = submissionRepository.findByProblemIdAndStatusNot(
                problem.getId(), SubmissionStatus.ERROR);

        if (submissions.isEmpty()) return;

        double avg = submissions.stream()
                .mapToInt(Submission::getTotalUserTokens)
                .average()
                .orElse(0.0);

        problem.setAvgUserTokens((float) avg);
        problemRepository.save(problem);
    }
}