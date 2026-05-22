package com.howprom.controller.submission;

import com.howprom.submission.dto.MyPageDetailResponseDto;
import com.howprom.submission.dto.MyPageResponseDto;
import com.howprom.submission.service.MyPageService;
import com.howprom.user.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    /**
     * 마이페이지 메인 데이터 조회 API (통계 + 페이징 목록)
     * GET /api/mypage?page=0&size=5
     */
    @GetMapping
    public ResponseEntity<MyPageResponseDto> getMyPageData(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PageableDefault(size = 5, sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        MyPageResponseDto response = myPageService.getMyPageData(principal.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 제출 기록 단건 상세 조회 API (상세 팝업용 대화 이력 포함)
     * GET /api/mypage/submissions/1
     */
    @GetMapping("/submissions/{id}")
    public ResponseEntity<MyPageDetailResponseDto> getSubmissionDetail(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable("id") Long submissionId) {

        MyPageDetailResponseDto response = myPageService.getSubmissionDetail(submissionId, principal.getId());
        return ResponseEntity.ok(response);
    }
}