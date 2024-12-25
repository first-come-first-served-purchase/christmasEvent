package com.doosan.christmas.service;

import com.doosan.christmas.util.RedisUtil;
import com.doosan.christmas.dto.responsedto.ResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final RedisUtil redisUtil;

    @Autowired
    private MailSendService mailSendService;

    public AuthService(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    public Boolean checkAuthNum(@Email @NotEmpty(message = "이메일을 입력해 주세요") String email,
                                @NotEmpty(message = "인증 번호를 입력해 주세요") String authNum) {
        logger.info("인증 번호 검증 시작 - 이메일: {}, 인증 번호: {}", email, authNum);

        // Redis에서 인증 번호 조회
        String storedEmail = redisUtil.getData(authNum);

        if (storedEmail == null) {
            logger.warn("인증 번호가 Redis에 존재하지 않음 - 인증 번호: {}", authNum);
            return false;
        }

        logger.debug("Redis에서 조회된 이메일: {}", storedEmail);

        // Redis에 저장된 이메일과 입력받은 이메일이 일치하는지 확인
        boolean isMatch = storedEmail.equals(email);

        if (isMatch) {
            logger.info("인증 성공 - 이메일: {}", email);
        } else {
            logger.warn("인증 실패 - 입력 이메일: {}, 저장된 이메일: {}", email, storedEmail);
        }
        return isMatch;
    }

    public String sendAuthEmail(String email) {
        logger.info("이메일 인증 코드 발송 시작 - 이메일: {}", email);
        String authCode = mailSendService.sendAuthEmail(email);
        logger.info("이메일 인증 코드 발송 완료 - 이메일: {}", email);
        return authCode;
    }

    public ResponseDto<Object> verifyEmailCode(String email, String authCode) {
        boolean isVerified = checkAuthNum(email, authCode);
        if (isVerified) {
            return ResponseDto.success(true);
        } else {
            return ResponseDto.fail("VERIFICATION_FAILED", "이메일 인증에 실패했습니다.");
        }
    }
}
