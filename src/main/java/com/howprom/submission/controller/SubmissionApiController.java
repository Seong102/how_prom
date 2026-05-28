package com.howprom.submission.controller;

import com.howprom.common.entity.User;
import com.howprom.repository.UserRepository;
import com.howprom.submission.dto.SubmitRequest;
import com.howprom.submission.service.SubmissionService;
import com.howprom.user.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionApiController {

    private final SubmissionService submissionService;
    private final UserRepository    userRepository;

    /**
     * SCR-PROB-02 → SCR-PROB-03
     * 대화 종료 및 제출 → 채점 → submission ID 반환
     * 프론트는 응답받은 ID로 /submission/{id}/review 로 이동
     */
    @PostMapping
    public ResponseEntity<?> submit(
            @RequestBody SubmitRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        try {
            User user = userRepository.findById(principal.getId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            Long submissionId = submissionService.submit(request, user);
            return ResponseEntity.ok(Map.of("submissionId", submissionId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("제출 처리 중 서버 오류", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "채점 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
        }
    }
}