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

        model.addAttribute("stats", dashboardStats);
        return "admin/AdminDashboard";
    }
}