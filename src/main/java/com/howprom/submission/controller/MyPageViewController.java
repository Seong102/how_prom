package com.howprom.submission.controller;

import com.howprom.submission.dto.MyPageResponseDto;
import com.howprom.submission.service.MyPageService;
import com.howprom.user.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageViewController {

    private final MyPageService myPageService;

    @GetMapping
    public String getMyPageHTML(@AuthenticationPrincipal CustomUserPrincipal principal,
                                Pageable pageable, Model model) {

        MyPageResponseDto myPageData = myPageService.getMyPageData(principal.getId(), pageable);

        model.addAttribute("stats", myPageData.getStats());
        model.addAttribute("submissions", myPageData.getSubmissions().getContent());
        model.addAttribute("page", myPageData.getSubmissions());
        model.addAttribute("username", principal.getNickname());
        return "mypage/mypage";
    }
}