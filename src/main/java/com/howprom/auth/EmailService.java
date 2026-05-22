package com.howprom.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${howprom.mail.from}")
    private String fromAddress;

    public void sendTempPassword(String toEmail, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("[HowProm] 임시 비밀번호 안내");
        message.setText(buildTempPasswordBody(tempPassword));
        mailSender.send(message);
    }

    private String buildTempPasswordBody(String tempPassword) {
        return """
                안녕하세요, HowProm 입니다.

                요청하신 임시 비밀번호를 안내드립니다.

                임시 비밀번호: %s

                로그인 후 보안을 위해 비밀번호를 변경해주세요.

                감사합니다.
                """.formatted(tempPassword);
    }
}
