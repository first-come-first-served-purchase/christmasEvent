package com.doosan.christmas.member.service;

import com.doosan.christmas.common.util.RedisUtil;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MailSendService mailSendService;
    private final RedisUtil redisUtil;

    /**
     * 이메일 인증 코드 발송
     * @param email 이메일 주소
     * @return 인증 코드
     */
    public String sendAuthEmail(String email) {
        log.info("이메일 인증 코드 발송 요청 시작 - 이메일: {}", email);

        try {
            // 인증 코드 생성 및 발송
            String authCode = mailSendService.sendAuthEmail(email);
            log.info("이메일 인증 코드 발송 성공 - 이메일: {}, 인증 코드: {}", email, authCode);

            return authCode;
        } catch (Exception e) {
            log.error("이메일 인증 코드 발송 실패 - 이메일: {}, 오류: {}", email, e.getMessage(), e);
            throw new RuntimeException("이메일 인증 코드 발송 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 인증 번호 확인
     * @param email 이메일 주소
     * @param authNum 인증 코드
     * @return 인증 성공 여부
     */
    public boolean checkAuthNum(String email, String authNum) {
        log.info("인증 번호 확인 시작 - 이메일: {}, 인증 번호: {}", email, authNum);

        String savedEmail = redisUtil.getData(authNum);
        log.debug("Redis 조회 결과 - 인증 번호: {}, 저장된 이메일: {}", authNum, savedEmail);

        if (email.equals(savedEmail)) {
            // 인증 성공 시 이메일 키로 "VERIFIED" 상태 저장
            redisUtil.setDataExpire(email, "VERIFIED", 3600); // 인증 상태 1시간 유지
            log.info("인증 번호 확인 성공 - 이메일: {}", email);
            return true;
        }

        log.warn("인증 번호 확인 실패 - 이메일: {}, 저장된 이메일: {}", email, savedEmail);
        return false;
    }



    /**
     * 이메일 인증 여부 확인
     * @param email 이메일 주소
     * @return 인증 완료 여부
     */
    public boolean isEmailVerified(String email) {
        log.info("이메일 인증 여부 확인 시작 - 이메일: {}", email);

        // Redis에서 인증 상태 조회
        String status = redisUtil.getData(email);
        log.debug("Redis 조회 결과 - 이메일: {}, 상태: {}", email, status);

        boolean isVerified = "VERIFIED".equals(status);
        if (isVerified) {
            log.info("이메일 인증 상태 확인 완료 - 이메일: {}, 인증 상태: VERIFIED", email);
        } else {
            log.warn("이메일 인증 상태 확인 실패 - 이메일: {}, 상태: {}", email, status);
        }

        return isVerified;
    }
}
