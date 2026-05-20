package com.howprom.controller.problem;
 
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
 *   GET /result/{id}       → 채점 결과 (SCR-PROB-03)
 */
@Controller
@RequestMapping("/problems")
public class ProblemController {
 
    /**
     * SCR-PROB-01: 문제 목록
     * URL: GET /problems
     * 템플릿: templates/problem/problemList.html
     */
    @GetMapping
    public String problemList(Model model) {
        // TODO: DB에서 공개 문제 목록 조회 후 model에 추가
        // List<ProblemDto> problems = problemService.getPublicProblems();
        // model.addAttribute("problems", problems);
        return "problem/problemList";
    }
 
    /**
     * SCR-PROB-02: 문제 상세 · 풀기 (채팅 UI)
     * URL: GET /problems/{id}
     * 템플릿: templates/problem/problemDetail.html
     *
     * Model에 담아야 할 DB 데이터:
     *   problem.id              → problems.id
     *   problem.title           → problems.title
     *   problem.description     → problems.description
     *   problem.exampleInput    → problems.example_input  (nullable)
     *   problem.exampleOutput   → problems.example_output (nullable)
     *   problem.evaluationType  → problems.evaluation_type (STANDARD/EFFICIENCY/BUDGET)
     *   problem.tokenLimit      → problems.token_limit (nullable, BUDGET 모드만)
     *   problem.correctnessWeight → problems.correctness_weight (EFFICIENCY 모드)
     *   problem.efficiencyWeight  → problems.efficiency_weight  (EFFICIENCY 모드)
     *   requirements            → requirements 테이블 (display_order ASC)
     */
    @GetMapping("/{id}")
    public String problemDetail(@PathVariable Long id, Model model) {
        // TODO: 실제 DB 연동 시 아래 주석 해제
        // ProblemDto problem = problemService.getProblem(id);
        // List<RequirementDto> requirements = requirementService.getByProblemId(id);
        // model.addAttribute("problem", problem);
        // model.addAttribute("requirements", requirements);
 
        // ── 임시 더미 데이터 (DB 연동 전 화면 확인용) ──
        model.addAttribute("problemId", id);
 
        return "problem/problemDetail";
    }
}