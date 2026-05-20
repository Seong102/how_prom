package com.howprom.admin.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.howprom.admin.entity.Problem;
import com.howprom.admin.service.AdminProblemService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin") 
public class AdminProblemController {

    private final AdminProblemService adminProblemService;

    // 2. 관리자 메인 대시보드 매핑
    @GetMapping("") 
    public String adminDashboard() {
        return "admin/AdminDashboard";
    }

    // 3. 문제 관리 페이지 (검색 기능 통합 버전)
    // 기존의 파라미터 없는 메서드와 검색용 메서드를 하나로 통합했습니다.
    @GetMapping("/problems/manage")
    public String manageProblems(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        // 서비스에서 검색어에 맞는 데이터를 가져옵니다. (검색어가 null이면 전체 조회)
        List<Problem> realProblemList = adminProblemService.getProblems(keyword);
        
        model.addAttribute("problemList", realProblemList);
        model.addAttribute("keyword", keyword); // 검색창에 검색어 유지

        return "admin/problems/manage";
    }
    
    // 4. 문제 등록 페이지 이동
    @GetMapping("/problems/register")
    public String registerForm() {
        return "admin/problems/register";
    }

    // 5. 문제 등록 처리
    @PostMapping("/problems/register")
    public String registerProcess() {
        return "redirect:/admin/problems/manage";
    }

    // 6. 문제 삭제 비동기 처리 API
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

    // 7. 공개 여부 토글 비동기 처리 API
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