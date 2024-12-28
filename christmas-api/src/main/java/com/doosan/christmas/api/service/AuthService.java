package com.doosan.christmas.api.service;

import com.doosan.christmas.common.util.RedisUtil;
import com.doosan.christmas.common.exception.CustomException;
import com.doosan.christmas.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final RedisUtil redisUtil;
    private final EmailService emailService;
    
    public boolean isEmailVerified(String email) {
        String status = redisUtil.getData(email);
        return "VERIFIED".equals(status);
    }
    
    public void sendEmail(String email) {
        emailService.sendVerificationEmail(email);
    }
    
    public void verifyCode(String email, String code) {
        if (!emailService.verifyEmail(email, code)) {
            throw new CustomException(ErrorCode.INVALID_VERIFICATION_CODE);
        }
    }
} 