package com.howprom.problem.controller;
 
import com.howprom.problem.dto.ProblemDetailDto;
import com.howprom.problem.service.ProblemDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
 
/**
 * 문제 관련 화면 라우팅 컨트롤러
 *
 * URL 구조:
 *   GET /problems          → 문제 목록 (SCR-PROB-01)
 *   GET /problems/{id}     → 문제 상세 · 풀기 (SCR-PROB-02)
 */
@Controller
@RequestMapping("/problems")
@RequiredArgsConstructor
public class ProblemController {
 
    private final ProblemDetailService problemDetailService;
 
    /**
     * SCR-PROB-01: 문제 목록
     * URL: GET /problems
     * 템플릿: templates/problem/problemList.html
     */
    @GetMapping
    public String problemList(Model model) {
        // TODO: 문제 목록 기능 구현 시 추가
        return "problem/problemList";
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