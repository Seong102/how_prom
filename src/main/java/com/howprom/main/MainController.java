package com.howprom.main;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MainController {

    @GetMapping({"/", "/main"})
    public String main(Model model) {
        // 통계 카드 데이터
        Map<String, Object> stats = new HashMap<>();
        stats.put("solvedCount", 42);
        stats.put("successRate", 78);
        stats.put("avgTokens", 312);
        model.addAttribute("stats", stats);

        // 최근 제출 내역
        List<Map<String, Object>> recentSubmissions = List.of(
                submission("이메일 요약 프롬프트 작성", 92, LocalDate.of(2025, 1, 15), true),
                submission("코드 리뷰 프롬프트", 75, LocalDate.of(2025, 1, 14), true),
                submission("데이터 분석 프롬프트", 45, LocalDate.of(2025, 1, 13), false),
                submission("번역 프롬프트 최적화", 88, LocalDate.of(2025, 1, 12), true)
        );
        model.addAttribute("recentSubmissions", recentSubmissions);

        return "main/main";
    }

    private Map<String, Object> submission(String name, int score, LocalDate date, boolean passed) {
        Map<String, Object> row = new HashMap<>();
        row.put("problemName", name);
        row.put("score", score);
        row.put("submittedAt", date);
        row.put("passed", passed);
        return row;
    }
}
