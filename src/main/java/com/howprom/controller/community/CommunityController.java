package com.howprom.controller.community;

import com.howprom.community.dto.CommunityListDto;
import com.howprom.community.service.CommunityService;
import com.howprom.submission.dto.MyPageDetailResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    /**
     * 다른 풀이 보기 목록 페이지
     * GET /community/problems/{problemId}?excludeUserId=xxx
     */
    @GetMapping("/community/problems/{problemId}")
    public String communityList(@PathVariable Long problemId,
                                @RequestParam(defaultValue = "0") Long excludeUserId,
                                Model model) {
        List<CommunityListDto> submissions = communityService.getCommunityList(problemId, excludeUserId);
        model.addAttribute("submissions", submissions);
        model.addAttribute("problemId", problemId);
        return "community/community";
    }

    /**
     * 다른 유저의 풀이 상세 보기
     * GET /community/submissions/{submissionId}
     */
    @GetMapping("/community/submissions/{submissionId}")
    public String communityDetail(@PathVariable Long submissionId, Model model) {
        MyPageDetailResponseDto detail = communityService.getPublicSubmissionDetail(submissionId);
        model.addAttribute("detail", detail);
        model.addAttribute("submissionId", submissionId);
        return "submission/review";
    }
}
