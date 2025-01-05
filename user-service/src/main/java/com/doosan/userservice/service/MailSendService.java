package com.doosan.userservice.service;

import com.doosan.userservice.util.RedisUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailSendService {

    private final RedisUtil redisUtil;

    private final JavaMailSender mailSender;

    private String authNum;

    // 인증번호 생성
    public void createCode() {
        Random random = new Random();
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            key.append(random.nextInt(10));
        }
        authNum = key.toString();
    }

    // 메일 양식 작성
    public MimeMessage createEmailForm(String email) throws MessagingException {
        createCode();
        String setFrom = "doosan00425@naver.com";
        String title = "[크리스마스마켓] 이메일 인증을 완료해 주세요";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(setFrom);
        helper.setTo(email);
        helper.setSubject(title);

        String msgOfEmail = String.format("""
            <div style='max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif;'>
                <div style='text-align: center; margin-bottom: 30px;'>
                    <h1 style='color: #D4272D; margin-bottom: 10px;'>크리스마스마켓</h1>
                    <p style='color: #666; font-size: 16px; margin-bottom: 30px;'>
                        이메일 주소 인증을 위한 인증번호입니다.
                    </p>
                </div>
                
                <div style='background-color: #f8f9fa; border-radius: 10px; padding: 30px; text-align: center; margin-bottom: 30px;'>
                    <p style='color: #333; font-size: 18px; margin-bottom: 20px;'>인증번호</p>
                    <div style='background: #fff; padding: 15px; border-radius: 5px; border: 1px solid #ddd; margin-bottom: 20px;'>
                        <span style='color: #D4272D; font-size: 24px; font-weight: bold; letter-spacing: 3px;'>%s</span>
                    </div>
                    <p style='color: #999; font-size: 14px;'>인증번호는 10분간 유효합니다.</p>
                </div>
                
                <div style='border-top: 1px solid #eee; padding-top: 20px;'>
                    <p style='color: #666; font-size: 14px; line-height: 1.5; margin-bottom: 10px;'>
                        ※ 본 메일은 발신전용이며, 문의사항은 고객센터를 이용해 주시기 바랍니다.<br>
                        ※ 인증번호를 요청하지 않았다면 본 메일을 무시해주세요.
                    </p>
                    <p style='color: #999; font-size: 13px;'>
                        © 2024 크리스마스마켓. All rights reserved.
                    </p>
                </div>
            </div>
        """, authNum);

        helper.setText(msgOfEmail, true);
        return message;
    }

    public String sendAuthEmail(String email) {
        try {
            MimeMessage message = createEmailForm(email);
            mailSender.send(message);
            log.info("인증 이메일 전송 성공: {}", email);

            redisUtil.setDataExpire(authNum, email, 600);
            log.info("Redis에 인증 번호 저장 완료: key={}, email={}", authNum, email);

            return authNum;

        } catch (MessagingException e) {
            log.error("인증 이메일 전송 실패: {}", e.getMessage());
            throw new IllegalStateException("이메일 전송 중 오류가 발생했습니다.", e);
        }
    }
}
