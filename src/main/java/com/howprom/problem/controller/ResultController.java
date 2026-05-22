package com.howprom.problem.controller;

import com.howprom.submission.dto.RequirementResultViewDto;
import com.howprom.submission.dto.ResultViewDto;
import com.howprom.submission.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

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

    /** 채점 상태 폴링용 API — 페이지 전체 로드 없이 status 문자열만 반환 */
    @GetMapping("/api/submissions/{submissionId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getStatus(@PathVariable Long submissionId) {
        ResultViewDto dto = resultService.getResultView(submissionId);
        return ResponseEntity.ok(Map.of("status", dto.getStatus()));
    }
}
