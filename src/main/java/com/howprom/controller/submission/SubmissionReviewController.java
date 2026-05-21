package com.howprom.controller.submission;

import com.howprom.submission.dto.MyPageDetailResponseDto;
import com.howprom.submission.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class SubmissionReviewController {

    private final MyPageService myPageService;

    @GetMapping("/submission/{submissionId}/review")
    public String review(@PathVariable Long submissionId, Model model) {
        Long mockUserId = 2L;
        MyPageDetailResponseDto detail = myPageService.getSubmissionDetail(submissionId, mockUserId);
        model.addAttribute("detail", detail);
        model.addAttribute("submissionId", submissionId);
        return "submission/review";
    }
}
