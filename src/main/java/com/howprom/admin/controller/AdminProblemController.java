package com.howprom.admin.controller;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpSession; 
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.howprom.common.entity.Problem;
import com.howprom.common.entity.User;
import com.howprom.admin.dto.ProblemAdminDTO;
import com.howprom.admin.dto.ProblemStatsDTO;
import com.howprom.admin.service.AdminProblemService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin") 
@PreAuthorize("hasRole('ADMIN')")
public class AdminProblemController {

    private final AdminProblemService adminProblemService;
    
    // 문제 관리 페이지: 목록 및 통계 데이터 조회
    @GetMapping("/problems/manage")
    public String manageProblems(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        // 1. 전체 문제 조회
        List<Problem> realProblemList = adminProblemService.getProblems(keyword);
        
        // 2. 문제 엔티티를 DTO로 변환 (★ 이때 DTO 내부에 requirements 리스트가 바인딩되어 있어야 조건 개수 출력 가능)
        List<ProblemAdminDTO> problemDtoList = realProblemList.stream()
                .map(ProblemAdminDTO::from)
                .toList();
        
        // 3. 통계 데이터 Map 조회 (★ 서비스 레이어에서 각 DTO 내부에 passRate 계산 로직이 포함되어 반환됨)
        Map<Long, ProblemStatsDTO> statsMap = adminProblemService.getStatsMap();
        
        // 4. 뷰 템플릿(Thymeleaf)으로 데이터 전송
        model.addAttribute("problemList", problemDtoList);
        model.addAttribute("statsMap", statsMap);
        model.addAttribute("keyword", keyword); 

        return "admin/problems/manage";
    }
    
    // 문제 등록 페이지 이동
    @GetMapping("/problems/register")
    public String registerForm(Model model) {
        ProblemAdminDTO dto = new ProblemAdminDTO();
        dto.setRequirements(new java.util.ArrayList<>()); 
        model.addAttribute("problem", dto);
        return "admin/problems/register";
    }

    // 문제 등록 처리
    @PostMapping("/problems/register")
    public String registerProcess(@ModelAttribute ProblemAdminDTO dto,
                                  @RequestParam(value = "reqDesc", required = false) List<String> reqDescs,
                                  @RequestParam(value = "reqWeight", required = false) List<Integer> reqWeights,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        try {
            User loginUser = (User) session.getAttribute("loginUser");
            if (loginUser == null) {
                loginUser = User.builder().id(1L).email("admin@howprom.com").nickname("최고관리자").build();
            }
            adminProblemService.createProblem(dto, reqDescs, reqWeights, loginUser);
            redirectAttributes.addFlashAttribute("message", "새 문제가 성공적으로 등록되었습니다!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "문제 등록 실패: " + e.getMessage());
        }
        return "redirect:/admin/problems/manage";
    }
    
    // 문제 수정 페이지 이동
    @GetMapping("/problems/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Problem problem = adminProblemService.findById(id);
        model.addAttribute("problem", ProblemAdminDTO.from(problem));
        model.addAttribute("existingRequirements", problem.getRequirements());
        return "admin/problems/register";
    }

    // 문제 수정 처리
    @PostMapping("/problems/edit/{id}")
    public String editProcess(@PathVariable("id") Long id,
                              @ModelAttribute ProblemAdminDTO dto,
                              @RequestParam(value = "reqDesc", required = false) List<String> reqDescs,
                              @RequestParam(value = "reqWeight", required = false) List<Integer> reqWeights,
                              RedirectAttributes redirectAttributes) {
        try {
            adminProblemService.updateProblem(id, dto, reqDescs, reqWeights);
            redirectAttributes.addFlashAttribute("message", "문제 수정이 완료되었습니다!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "문제 수정 실패: " + e.getMessage());
        }
        return "redirect:/admin/problems/manage";
    }

    // 문제 삭제 처리
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

    // 문제 공개/비공개 토글 처리
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