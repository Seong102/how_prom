package com.howprom.domain.submission.controller;

import com.howprom.domain.submission.dto.MyPageDetailResponseDto;
import com.howprom.domain.submission.dto.MyPageResponseDto;
import com.howprom.domain.submission.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
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
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Long mockUserId = 2L; // 💡 로그인 연동 전 임시 하드코딩 유저 ID
        MyPageResponseDto response = myPageService.getMyPageData(mockUserId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 제출 기록 단건 상세 조회 API (상세 팝업용 대화 이력 포함)
     * GET /api/mypage/submissions/1
     */
    @GetMapping("/submissions/{id}")
    public ResponseEntity<MyPageDetailResponseDto> getSubmissionDetail(@PathVariable("id") Long submissionId) {

        Long mockUserId = 2L; // 💡 본인 확인 인가 검증용 임시 유저 ID
        MyPageDetailResponseDto response = myPageService.getSubmissionDetail(submissionId, mockUserId);
        return ResponseEntity.ok(response);
    }
}