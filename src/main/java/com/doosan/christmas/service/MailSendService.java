package com.doosan.christmas.service;

import com.doosan.christmas.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Random;

@Service
public class MailSendService {

    private static final Logger logger = LoggerFactory.getLogger(MailSendService.class);

    private final RedisUtil redisUtil;
    private final JavaMailSender mailSender;

    private String authNum; // 인증 번호

    @Autowired
    public MailSendService(RedisUtil redisUtil, JavaMailSender mailSender) {
        this.redisUtil = redisUtil;
        this.mailSender = mailSender;
    }

    // 인증번호 생성
    public void createCode() {
        Random random = new Random();
        StringBuffer key = new StringBuffer();

        for (int i = 0; i < 8; i++) {
            key.append(random.nextInt(10));
        }

        authNum = key.toString();
    }

    // 메일 양식 작성
    public MimeMessage createEmailForm(String email) throws MessagingException {
        createCode(); // 인증 코드 생성
        String setFrom = "doosan00425@naver.com"; // 발신자 메일
        String title = "회원가입 인증 번호"; // 제목

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(setFrom); // 발신자 설정
        helper.setTo(email);     // 수신자 설정
        helper.setSubject(title);// 제목 설정
        
        String msgOfEmail = "";
        msgOfEmail += "<div style='margin:20px;'>";
        msgOfEmail += "<h1> 안녕하세요 크리스마스 프로젝트입니다. </h1>";
        msgOfEmail += "<br>";
        msgOfEmail += "<p>아래 코드를 입력해주세요<p>";
        msgOfEmail += "<br>";
        msgOfEmail += "<div align='center' style='border:1px solid black; font-family:verdana';>";
        msgOfEmail += "<h3 style='color:blue;'>회원가입 인증 코드입니다.</h3>";
        msgOfEmail += "<div style='font-size:130%'>";
        msgOfEmail += "CODE : <strong>";
        msgOfEmail += authNum + "</strong><div><br/> ";
        msgOfEmail += "</div>";
        
        helper.setText(msgOfEmail, true);  // HTML 형식으로 설정

        return message;
    }

    public String sendAuthEmail(String email) {
        try {
            MimeMessage message = createEmailForm(email);
            mailSender.send(message);
            logger.info("인증 이메일 전송 성공: {}", email);

            redisUtil.setDataExpire(String.valueOf(authNum), email, 600);
            logger.info("Redis에 인증 번호 저장 완료: key={}, email={}", authNum, email);

            return String.valueOf(authNum);

        } catch (MessagingException e) {
            logger.error("인증 이메일 전송 실패: {}", e.getMessage());
            throw new IllegalStateException("이메일 전송 중 오류가 발생했습니다.");
        }
    }
}
