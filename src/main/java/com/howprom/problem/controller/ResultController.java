package com.howprom.problem.controller;

import com.howprom.submission.dto.RequirementResultViewDto;
import com.howprom.submission.dto.ResultViewDto;
import com.howprom.submission.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;

    @GetMapping("/result/{submissionId}")
    public String result(@PathVariable Long submissionId, Model model) {
        ResultViewDto submission = resultService.getResultView(submissionId);
        List<RequirementResultViewDto> requirementsResult = resultService.getRequirementResults(submissionId);

        model.addAttribute("submission", submission);
        model.addAttribute("requirementsResult", requirementsResult);
        model.addAttribute("submissionId", submissionId);

        return "problem/result";
    }
}
