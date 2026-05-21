package com.howprom.problem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ResultController {

    /**
     * GET /result/{submissionId}
     * 템플릿: templates/problem/result.html
     *
     * DB 연동 후 필요한 데이터 (submissions + problems 테이블):
     *   submission.score            → submissions.score
     *   submission.status           → submissions.status (PASSED/FAILED/ERROR)
     *   submission.feedback         → submissions.feedback
     *   submission.totalUserTokens  → submissions.total_user_tokens
     *   submission.tokenLimit       → problems.token_limit
     *   submission.turnCount        → submissions.conversation JSON 배열 길이
     *   submission.evaluationType   → problems.evaluation_type
     *   submission.problemTitle     → problems.title
     *   requirementsResult          → submissions.requirements_result JSON
     *                                 + requirements.description, weight 조인
     */
    @GetMapping("/result/{submissionId}")
    public String result(@PathVariable Long submissionId, Model model) {

        // ┌──────────────────────────────────────────────────────────┐
        // │  더미 데이터 (DB 연동 전 테스트용)                         │
        // │  DB 연동 후 → SubmissionService.getResult(submissionId)  │
        // └──────────────────────────────────────────────────────────┘

        // submissions + problems 조인 결과 → ${submission.XXX}
        Map<String, Object> submission = new HashMap<>();
        submission.put("score",           88);
        submission.put("status",          "PASSED");
        submission.put("feedback",        "버블정렬의 핵심 구조를 정확히 파악하고 조기 종료(swapped 플래그)를 올바르게 구현했습니다. " +
                                          "함수 분리는 완료되었으나 docstring이나 타입 힌트가 있으면 더 좋은 코드가 됩니다. " +
                                          "전반적으로 요구사항을 잘 이해하고 효율적으로 구현한 풀이입니다.");
        submission.put("totalUserTokens", 487);
        submission.put("tokenLimit",      1500);
        submission.put("turnCount",       4);
        submission.put("evaluationType",  "BUDGET");    // STANDARD | EFFICIENCY | BUDGET
        submission.put("problemTitle",    "버블 정렬 알고리즘 구현");
        model.addAttribute("submission", submission);

        // submissions.requirements_result JSON + requirements 테이블 조인
        // → ${requirementsResult} (th:each 루프)
        List<Map<String, Object>> requirementsResult = List.of(
            Map.of("description", "이중 루프 구조 사용",    "score", 30, "maxScore", 30, "pct", 100),
            Map.of("description", "조기 종료 최적화",       "score", 36, "maxScore", 40, "pct", 90),
            Map.of("description", "함수로 분리 (def)",      "score", 22, "maxScore", 30, "pct", 73)
        );
        model.addAttribute("requirementsResult", requirementsResult);

        return "problem/result";
    }
}
