package com.howprom.auth;


import com.howprom.user.dto.SignupRequest;
import com.howprom.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/auth/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/auth/signup")
    public String signupPage() {
        return "auth/signup";
    }

    @PostMapping("/auth/signup")
    public String signup(@Valid @ModelAttribute SignupRequest request,
                         BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String firstError = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return "redirect:/auth/signup?error=" + encode(firstError);
        }
        try {
            userService.signup(request);
        } catch (IllegalArgumentException e) {
            return "redirect:/auth/signup?error=" + encode(e.getMessage());
        }
        return "redirect:/auth/login?signup";
    }

    // 이메일 중복 확인 API (signup.js에서 호출)
    @GetMapping("/auth/check-email")
    @ResponseBody
    public boolean checkEmail(@RequestParam String email) {
        return userService.isEmailAvailable(email);
    }

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}