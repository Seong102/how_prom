package com.howprom.auth;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

	// 로그인 페이지 이동
    @GetMapping("/auth/login")
    public String loginPage() {
        return "auth/login";
    }
    
    // 회원가입 페이지 이동
    @GetMapping("/auth/signup")
    public String signupPage() {
        return "auth/signup"; 
    }
}