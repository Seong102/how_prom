package com.howprom.submission.controller;

import com.howprom.submission.dto.MyPageDetailResponseDto;
import com.howprom.submission.service.MyPageService;
import com.howprom.user.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class SubmissionReviewController {

    private final MyPageService myPageService;

    @GetMapping("/submission/{submissionId}/review")
    public String review(@AuthenticationPrincipal CustomUserPrincipal principal,
                         @PathVariable Long submissionId, Model model) {
        MyPageDetailResponseDto detail = myPageService.getSubmissionDetail(submissionId, principal.getId());
        model.addAttribute("detail", detail);
        model.addAttribute("submissionId", submissionId);
        return "submission/review";
    }
}
