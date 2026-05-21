package com.howprom.admin.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.howprom.common.entity.Problem;
import com.howprom.admin.dto.ProblemAdminDTO;
import com.howprom.admin.dto.ProblemStatsDTO;
import com.howprom.admin.service.AdminProblemService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin") 
public class AdminProblemController {

    private final AdminProblemService adminProblemService;

    @GetMapping("") 
    public String adminDashboard() {
        return "admin/AdminDashboard";
    }

    // 문제 관리 페이지: 목록 및 통계 데이터 조회
    @GetMapping("/problems/manage")
    public String manageProblems(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<Problem> realProblemList = adminProblemService.getProblems(keyword);
        
        List<ProblemAdminDTO> problemDtoList = realProblemList.stream()
                .map(ProblemAdminDTO::from)
                .toList();
        
        Map<Long, ProblemStatsDTO> statsMap = adminProblemService.getStatsMap();
        
        model.addAttribute("problemList", problemDtoList);
        model.addAttribute("statsMap", statsMap);
        model.addAttribute("keyword", keyword); 

        return "admin/problems/manage";
    }
    
    // [수정] 문제 등록 페이지 이동: HTML 내부의 th:value="${problem...}" 에러 방지용 빈 객체 주입
    @GetMapping("/problems/register")
    public String registerForm(Model model) {
        model.addAttribute("problem", new ProblemAdminDTO());
        return "admin/problems/register";
    }

    // [수정] 문제 등록 처리: 폼 기본 정보와 동적 생성된 요구사항 리스트(reqDesc, reqWeight) 수집
    @PostMapping("/problems/register")
    public String registerProcess(@ModelAttribute ProblemAdminDTO dto,
                                  @RequestParam(value = "reqDesc", required = false) List<String> reqDescs,
                                  @RequestParam(value = "reqWeight", required = false) List<Integer> reqWeights) {
        adminProblemService.createProblem(dto, reqDescs, reqWeights);
        return "redirect:/admin/problems/manage";
    }
    
    // 문제 수정 페이지 이동 (기존 코드 유지)
    @GetMapping("/problems/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Problem problem = adminProblemService.findById(id);
        model.addAttribute("problem", ProblemAdminDTO.from(problem));
        model.addAttribute("existingRequirements", problem.getRequirements());
        return "admin/problems/register";
    }

    // [추가] 문제 수정 처리: html 폼의 액션 분기(@{/admin/problems/edit/{id}})를 받아 처리할 매핑
    @PostMapping("/problems/edit/{id}")
    public String editProcess(@PathVariable("id") Long id,
                              @ModelAttribute ProblemAdminDTO dto,
                              @RequestParam(value = "reqDesc", required = false) List<String> reqDescs,
                              @RequestParam(value = "reqWeight", required = false) List<Integer> reqWeights) {
        adminProblemService.updateProblem(id, dto, reqDescs, reqWeights);
        return "redirect:/admin/problems/manage";
    }

    @PostMapping("/problems/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteProblem(@PathVariable("id") Long id) {
        try {
            adminProblemService.deleteProblem(id);
            return ResponseEntity.ok("SUCCESS");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("FAIL");
        }
    }

    @PostMapping("/problems/toggle/{id}")
    @ResponseBody
    public ResponseEntity<String> toggleProblemStatus(
            @PathVariable("id") Long id, 
            @RequestParam("isPublic") boolean isPublic) {
        try {
            adminProblemService.updatePublicStatus(id, isPublic);
            return ResponseEntity.ok("SUCCESS");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("FAIL");
        }
    }
}