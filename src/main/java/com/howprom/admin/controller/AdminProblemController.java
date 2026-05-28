package com.howprom.admin.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// 🚨 [에러 해결] 누락되었던 User 엔티티 임포트를 추가했습니다!
import com.howprom.common.entity.User; 
import com.howprom.admin.dto.ProblemAdminDTO;
import com.howprom.admin.dto.ProblemStatsDTO;
import com.howprom.admin.service.AdminProblemService;
import com.howprom.user.CustomUserPrincipal;
import com.howprom.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin") 
@PreAuthorize("hasRole('ADMIN')")
public class AdminProblemController {

    private final AdminProblemService adminProblemService;
    private final UserRepository userRepository;
    
    /**
     * 문제 관리 페이지: 목록 및 통계 데이터 조회
     */
    @GetMapping("/problems/manage")
    public String manageProblems(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        // 서비스 단에서 트랜잭션을 탄 채로 안전하게 바인딩된 DTO 리스트를 받아옵니다 (Lazy 에러 방어)
        List<ProblemAdminDTO> problemDtoList = adminProblemService.getProblemsAsDTO(keyword);
        
        // 대시보드 통계용 맵 데이터 조회
        Map<Long, ProblemStatsDTO> statsMap = adminProblemService.getStatsMap();
        
        model.addAttribute("problemList", problemDtoList);
        model.addAttribute("statsMap", statsMap);
        model.addAttribute("keyword", keyword); 

        return "admin/problems/manage";
    }
    
    /**
     * 문제 등록 페이지 이동
     */
    @GetMapping("/problems/register")
    public String registerForm(Model model) {
        ProblemAdminDTO dto = new ProblemAdminDTO();
        dto.setRequirements(new java.util.ArrayList<>()); 
        model.addAttribute("problem", dto);
        return "admin/problems/register";
    }

    /**
     * 문제 등록 처리
     */
    @PostMapping("/problems/register")
    public String registerProcess(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @ModelAttribute ProblemAdminDTO dto,
            @RequestParam(value = "reqDesc", required = false) List<String> reqDescs,
            @RequestParam(value = "reqWeight", required = false) List<Integer> reqWeights,
            RedirectAttributes redirectAttributes) {
        try {
            User loginUser = userRepository.findById(principal.getId())
                    .orElseThrow(() -> new IllegalStateException("로그인 정보를 확인할 수 없습니다."));

            adminProblemService.createProblem(dto, reqDescs, reqWeights, loginUser);
            redirectAttributes.addFlashAttribute("message", "새 문제가 성공적으로 등록되었습니다!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "문제 등록 실패: " + e.getMessage());
        }
        return "redirect:/admin/problems/manage";
    }
    
    /**
     * 문제 수정 페이지 이동
     */
    @GetMapping("/problems/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        // 서비스로부터 Lazy 로딩이 완료된 완벽한 DTO를 한 번에 넘겨받음 (Lazy 에러 방어)
        ProblemAdminDTO problemDto = adminProblemService.getProblemDetailAsDTO(id);
        
        model.addAttribute("problem", problemDto);
        model.addAttribute("existingRequirements", problemDto.getRequirements());
        return "admin/problems/register";
    }

    /**
     * 문제 수정 처리
     */
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

    /**
     * 문제 삭제 처리
     */
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

    /**
     * 문제 공개/비공개 토글 처리
     */
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