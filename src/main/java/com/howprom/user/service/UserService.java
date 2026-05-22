package com.howprom.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.howprom.auth.EmailService;
import com.howprom.common.entity.User;
import com.howprom.common.entity.UserRole;
import com.howprom.repository.UserRepository;
import com.howprom.user.CustomUserPrincipal;
import com.howprom.user.dto.SignupRequest;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        return new CustomUserPrincipal(user);
    }
    
    @Transactional
    public void signup(SignupRequest request) {
        // 비밀번호 일치 확인
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        // 이메일 중복
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        // 닉네임 중복
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 별명입니다.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER);

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
    
    @Transactional
    public void resetPasswordAndSendEmail(String email) {
        // 사용자 조회 — 없어도 동일하게 응답 (이메일 enumeration 방지)
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return;  // 조용히 종료
        }

        // 임시 비밀번호 생성
        String tempPassword = generateTempPassword(10);

        // DB 비밀번호 즉시 업데이트
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        // 메일 발송
        emailService.sendTempPassword(user.getEmail(), tempPassword);
    }

    private String generateTempPassword(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}