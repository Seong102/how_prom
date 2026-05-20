package com.howprom.admin.controller;

import java.util.List;
import java.util.Map; // 추가됨
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.howprom.common.entity.Problem;
import com.howprom.admin.dto.ProblemAdminDTO;
import com.howprom.admin.dto.ProblemStatsDTO; // 추가됨
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

    // [수정됨] 문제 관리 페이지: 통계 데이터(statsMap)를 모델에 함께 추가
    @GetMapping("/problems/manage")
    public String manageProblems(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        // 1. 문제 목록 가져오기
        List<Problem> realProblemList = adminProblemService.getProblems(keyword);
        
        // 2. DTO 리스트로 변환
        List<ProblemAdminDTO> problemDtoList = realProblemList.stream()
                .map(ProblemAdminDTO::from)
                .toList();
        
        // 3. [핵심] 통계 데이터 Map 조회 (문제 ID별로 통계 정보를 담음)
        Map<Long, ProblemStatsDTO> statsMap = adminProblemService.getStatsMap();
        
        model.addAttribute("problemList", problemDtoList);
        model.addAttribute("statsMap", statsMap); // 타임리프에서 사용 가능하도록 모델에 추가
        model.addAttribute("keyword", keyword); 

        return "admin/problems/manage";
    }
    
    @GetMapping("/problems/register")
    public String registerForm() {
        return "admin/problems/register";
    }

    @PostMapping("/problems/register")
    public String registerProcess() {
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
    
    @GetMapping("/problems/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Problem problem = adminProblemService.findById(id);
        model.addAttribute("problem", ProblemAdminDTO.from(problem));
        return "admin/problems/register";
    }
}