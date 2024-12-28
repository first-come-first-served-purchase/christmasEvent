package com.doosan.christmas.api.service;

import com.doosan.christmas.common.exception.CustomException;
import com.doosan.christmas.common.exception.ErrorCode;
import com.doosan.christmas.common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final RedisUtil redisUtil;
    private static final long VERIFICATION_TIME = 300L; // 5분

    public String sendVerificationEmail(String email) {
        String verificationCode = generateVerificationCode();
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(email);
            helper.setSubject("이메일 인증 코드");
            helper.setText(createEmailContent(verificationCode), true);
            
            mailSender.send(message);
            
            // Redis에 인증 코드 저장 (5분 유효)
            redisUtil.setDataExpire(email, verificationCode, VERIFICATION_TIME);
            
            return verificationCode;
        } catch (MessagingException e) {
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    public boolean verifyEmail(String email, String code) {
        String savedCode = redisUtil.getData(email);
        if (savedCode == null) {
            throw new CustomException(ErrorCode.VERIFICATION_EXPIRED);
        }
        
        if (!savedCode.equals(code)) {
            throw new CustomException(ErrorCode.INVALID_VERIFICATION_CODE);
        }
        
        // 인증 성공 시 인증 완료 상태 저장
        redisUtil.setDataExpire(email + ":verified", "true", 24 * 60 * 60L); // 24시간 유효
        return true;
    }

    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    private String createEmailContent(String code) {
        return String.format("""
            <div style='margin:20px;'>
                <h1>이메일 인증 코드</h1>
                <p>아래의 인증 코드를 입력해주세요:</p>
                <div style='font-size:24px;font-weight:bold;color:#2196F3;'>
                    %s
                </div>
                <p>이 코드는 5분 동안만 유효합니다.</p>
            </div>
            """, code);
    }
} 