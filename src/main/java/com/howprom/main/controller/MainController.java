package com.howprom.main.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.howprom.main.service.DashboardService;
import com.howprom.user.CustomUserPrincipal;

import java.time.LocalTime;

@Controller
@RequiredArgsConstructor
public class MainController {
	
	private final DashboardService dashboardService;

    @GetMapping({"/", "/main"})
    public String main(@AuthenticationPrincipal CustomUserPrincipal principal,
                       Model model) {

        boolean isLoggedIn = (principal != null);
        model.addAttribute("isLoggedIn", isLoggedIn);

        // === 환영 메시지 ===
        if (isLoggedIn) {
            model.addAttribute("greeting",
                    buildGreeting(LocalTime.now().getHour(), principal.getNickname()));
        }
        // 비로그인이면 HTML에서 정적 문구 표시

        // === 로그인 사용자 전용 영역 ===
        if (isLoggedIn) {
            Long userId = principal.getId();
            model.addAttribute("continueProblem", dashboardService.getContinueProblem(userId));
            model.addAttribute("recommendedProblems", dashboardService.getRecommendedProblems(userId));
        }

        // === 공통 영역 (로그인/비로그인 모두) ===
        model.addAttribute("newProblems", dashboardService.getNewProblems());
        Long viewerId = (principal == null) ? null : principal.getId();
        model.addAttribute("topSubmissions", dashboardService.getTopWeeklySubmissions(viewerId));

        return "main/main";
    }

    // ===== 시간대별 인사 =====
    private String buildGreeting(int hour, String nickname) {
        String message;
        if (hour >= 6 && hour < 12)        message = "좋은 아침이에요";
        else if (hour >= 12 && hour < 18)  message = "오후도 화이팅이에요";
        else if (hour >= 18 && hour < 24)  message = "오늘도 수고하셨어요";
        else                                message = "야간 학습 중이시군요";
        return message + ", " + nickname + "님";
    }
}