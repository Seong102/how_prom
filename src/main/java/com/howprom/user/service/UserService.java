package com.howprom.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    
    /**
     * 닉네임 변경
     * - 본인 닉네임이면 변경 안 함
     * - 다른 사용자가 쓰는 닉네임이면 예외
     * - 변경 성공 시 현재 세션의 Principal도 갱신 (다음 페이지부터 새 닉네임 반영)
     */
    @Transactional
    public void updateNickname(Long userId, String newNickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 같은 닉네임이면 변경 불필요
        if (user.getNickname().equals(newNickname)) {
            return;
        }

        // 중복 확인
        if (userRepository.existsByNickname(newNickname)) {
            throw new IllegalArgumentException("이미 사용 중인 별명입니다.");
        }

        user.setNickname(newNickname);
        // @Transactional 안에서 엔티티 변경은 자동 반영 (dirty checking)

        // 현재 세션의 Principal 갱신 — 다음 페이지부터 새 닉네임 표시
        refreshSecurityContext(user);
    }

    /**
     * SecurityContext의 Authentication 객체를 새 User 정보로 갱신
     */
    private void refreshSecurityContext(User updatedUser) {
        Authentication oldAuth = SecurityContextHolder.getContext().getAuthentication();
        if (oldAuth == null) return;

        CustomUserPrincipal newPrincipal = new CustomUserPrincipal(updatedUser);
        UsernamePasswordAuthenticationToken newAuth =
                new UsernamePasswordAuthenticationToken(
                        newPrincipal,
                        oldAuth.getCredentials(),
                        oldAuth.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    /**
     * 비밀번호 변경
     * - 현재 비밀번호 일치 확인 후 새 비밀번호로 변경
     * - 새 비밀번호 ≠ 새 비밀번호 확인 → 예외
     * - 새 비밀번호 == 현재 비밀번호 → 예외 (의미 없음)
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword, String newPasswordConfirm) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 현재 비밀번호 일치 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 == 새 비밀번호 확인
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호가 현재 비밀번호와 동일하면 의미 없음
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호와 다른 비밀번호를 입력해주세요.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
    }
}