package com.doosan.christmas.user.service;

import com.doosan.christmas.common.exception.CustomException;
import com.doosan.christmas.common.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    public void sendVerificationEmail(String email, String verificationCode) {
        try {
            log.debug("[메일 작성] MimeMessage 객체 생성 시작");
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            log.debug("[메일 설정] 발신자: doosan00425@naver.com, 수신자: {}", email);
            helper.setFrom("doosan00425@naver.com");
            helper.setTo(email);
            helper.setSubject("크리스마스 이벤트 이메일 인증 코드");

            String emailContent = createEmailContent(verificationCode);
            log.debug("[메일 내용] HTML 형식의 이메일 본문 생성 완료");
            helper.setText(emailContent, true);

            log.info("[메일 전송] SMTP 서버로 전송 시도 - 수신자: {}", email);
            mailSender.send(message);
            log.info("[메일 전송] 성공적으로 전송 완료 - 수신자: {}", email);
            
        } catch (MessagingException e) {
            log.error("[메일 전송 실패] 수신자: {}, 오류 메시지: {}", email, e.getMessage());
            log.error("[상세 오류 정보]", e);
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }
    
    private String createEmailContent(String code) {
        log.debug("[메일 템플릿] HTML 템플릿 생성 시작 - 인증코드: {}", code);
        String content = String.format("""
            <div style='margin:20px;'>
                <h1>크리스마스 이벤트 이메일 인증</h1>
                <p>안녕하세요. 크리스마스 이벤트 인증코드를 보내드립니다.</p>
                <p>아래의 인증 코드를 입력해주세요:</p>
                <div style='font-size:24px;font-weight:bold;color:#2196F3;'>
                    %s
                </div>
                <p>이 인증코드는 5분 동안만 유효합니다.</p>
                <p>본 이메일은 발신전용이므로 회신이 불가능합니다.</p>
            </div>
            """, code);

        log.trace("[메일 템플릿] HTML 템플릿 생성 완료: {}", content);
        return content;
    }
} 