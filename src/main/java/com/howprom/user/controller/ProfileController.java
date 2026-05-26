package com.howprom.user.controller;

import com.howprom.user.CustomUserPrincipal;
import com.howprom.user.dto.ChangeNicknameRequest;
import com.howprom.user.dto.ChangePasswordRequest;
import com.howprom.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    /**
     * 닉네임 변경
     *
     * 응답 JSON:
     *   성공: { "success": true, "nickname": "새닉네임" }
     *   실패: { "success": false, "message": "에러 메시지" }
     */
    @PostMapping("/nickname")
    public ResponseEntity<Map<String, Object>> changeNickname(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ChangeNicknameRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return error(bindingResult.getFieldError().getDefaultMessage());
        }

        try {
            userService.updateNickname(principal.getId(), request.getNickname());
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("nickname", request.getNickname());
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    /**
     * 비밀번호 변경
     *
     * 응답 JSON:
     *   성공: { "success": true }
     *   실패: { "success": false, "message": "에러 메시지" }
     */
    @PostMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return error(bindingResult.getFieldError().getDefaultMessage());
        }

        try {
            userService.changePassword(
                    principal.getId(),
                    request.getCurrentPassword(),
                    request.getNewPassword(),
                    request.getNewPasswordConfirm());
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    private ResponseEntity<Map<String, Object>> error(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", message);
        return ResponseEntity.badRequest().body(body);
    }
}