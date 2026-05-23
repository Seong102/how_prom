package com.howprom.problem.controller;

import com.howprom.common.entity.EvaluationType;
import com.howprom.problem.dto.ProblemDetailDto;
import com.howprom.problem.dto.ProblemListItem;
import com.howprom.problem.service.ProblemDetailService;
import com.howprom.problem.service.ProblemListService;
import com.howprom.user.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemDetailService problemDetailService;
    private final ProblemListService problemListService;

    /**
     * SCR-PROB-01: 문제 목록
     *
     * 쿼리 파라미터:
     *   type   : STANDARD / EFFICIENCY / BUDGET (생략 시 전체)
     *   status : ALL / NOT_SOLVED / PASSED_ONLY (로그인 사용자만 의미 있음)
     *   sort   : latest (기본) / oldest
     *   page   : 0부터 시작
     */
    @GetMapping
    public String problemList(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false, defaultValue = "latest") String sort,
            @RequestParam(required = false, defaultValue = "0") int page,
            Model model) {

        Long userId = (principal == null) ? null : principal.getId();
        boolean isLoggedIn = (userId != null);

        EvaluationType evalType = parseEvalType(type);

        Page<ProblemListItem> result = problemListService.getList(
                userId, evalType, status, sort, page);

        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("problems", result.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("totalElements", result.getTotalElements());

        // 필터/정렬 현재 상태 (HTML에서 활성 표시용)
        model.addAttribute("currentType", type);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentSort", sort);

        return "problem/problemList";
    }

    private EvaluationType parseEvalType(String type) {
        if (type == null || type.isBlank() || "ALL".equalsIgnoreCase(type)) {
            return null;
        }
        try {
            return EvaluationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * SCR-PROB-02: 문제 상세 · 풀기 (채팅 UI)
     * URL: GET /problems/{id}
     * 템플릿: templates/problem/problemDetail.html
     */
    @GetMapping("/{id}")
    public String problemDetail(@PathVariable Long id, Model model) {
        ProblemDetailDto problem = problemDetailService.getProblemDetail(id);
        model.addAttribute("problem", problem);
        return "problem/problemDetail";
    }
}