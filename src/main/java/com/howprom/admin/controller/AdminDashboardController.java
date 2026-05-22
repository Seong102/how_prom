package com.howprom.admin.controller;

import com.howprom.admin.dto.AdminDashboardDTO;
import com.howprom.admin.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        AdminDashboardDTO dashboardStats = adminDashboardService.getDashboardStats();
        
        if (dashboardStats == null) {
            dashboardStats = AdminDashboardDTO.builder().build(); 
        }

        // 🔍 [긴급 점검 로그] 콘솔 창에서 null인지 빈 배열([])인지 확인하는 용도
        System.out.println("====== [대시보드 데이터 검증 로그] ======");
        System.out.println("1. 상단 지표 - 총 제출 건수: " + dashboardStats.getTotalSubmissions());
        System.out.println("2. 문제 통계 리스트: " + dashboardStats.getProblemList()); 
        System.out.println("3. 토큰 현황 리스트: " + dashboardStats.getEfficiencyList());
        System.out.println("4. 요구사항 리스트: " + dashboardStats.getRequirementList());
        System.out.println("=========================================");

        model.addAttribute("stats", dashboardStats);
        return "admin/AdminDashboard";
    }
}