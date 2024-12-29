package com.doosan.christmas.user.service;

import com.doosan.christmas.common.exception.CustomException;
import com.doosan.christmas.common.exception.ErrorCode;
import com.doosan.christmas.user.domain.Member;
import com.doosan.christmas.user.domain.Role;
import com.doosan.christmas.user.dto.SignupRequest;
import com.doosan.christmas.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.jsonwebtoken.Claims;
import com.doosan.christmas.user.security.TokenProvider;
import com.doosan.christmas.user.dto.TokenDto;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    
    private final EmailService emailService;
    private final RedisService redisService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    
    public void sendVerificationEmail(String email) {
        try {
            log.info("[이메일 인증] 프로세스 시작 - 수신자: {}", email);
            
            if (memberRepository.existsByEmail(email)) {
                throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
            }
            
            String verificationCode = generateVerificationCode();
            emailService.sendVerificationEmail(email, verificationCode);
            redisService.saveEmailVerification(email, verificationCode);
            
        } catch (Exception e) {
            log.error("[이메일 인증] 실패 - 수신자: {}, 오류: {}", email, e.getMessage());
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    private String generateVerificationCode() {
        log.debug("[인증코드 생성] 새로운 6자리 인증코드 생성 시작");
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append((int) (Math.random() * 10));
        }
        String generatedCode = code.toString();
        log.debug("[인증코드 생성] 생성 완료 - 인증코드: {}", generatedCode);
        return generatedCode;
    }

    public boolean verifyEmail(String email, String code) {
        String savedCode = redisService.getEmailVerification(email);
        
        if (savedCode == null) {
            throw new CustomException(ErrorCode.VERIFICATION_EXPIRED);
        }
        
        if (!savedCode.equals(code)) {
            throw new CustomException(ErrorCode.INVALID_VERIFICATION_CODE);
        }
        
        redisService.setVerifiedEmail(email);
        return true;
    }

    public void signup(Member member) {
        if (!redisService.isEmailVerified(member.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
        
        if (memberRepository.existsByEmail(member.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        
        member.encodePassword(passwordEncoder);
        memberRepository.save(member);
    }

    public Member login(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!member.matchPassword(passwordEncoder, password)) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        return member;
    }

    public TokenDto refreshToken(String refreshToken) {
        log.info("Refreshing token...");
        
        // 리프레시 토큰 검증
        if (!tokenProvider.validateToken(refreshToken)) {
            log.error("Invalid refresh token");
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        try {
            // 리프레시 토큰으로부터 사용자 정보 추출
            Claims claims = tokenProvider.validateAndGetClaims(refreshToken);
            String email = claims.getSubject();

            // Redis에서 저장된 리프레시 토큰 확인
            String savedRefreshToken = redisService.getRefreshToken(email);
            if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
                log.error("Refresh token not found in Redis or not matched");
                throw new CustomException(ErrorCode.INVALID_TOKEN);
            }

            // 새로운 토큰 발급
            Member member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            
            TokenDto newTokenDto = tokenProvider.generateToken(member);
            
            // 새로운 리프레시 토큰을 Redis에 저장
            redisService.saveRefreshToken(email, newTokenDto.getRefresh_token());
            
            log.info("Token refresh successful for user: {}", email);
            return newTokenDto;
            
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    public void logout(String accessToken) {
        // 토큰 유효성 검증
        if (!tokenProvider.validateToken(accessToken)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 토큰에서 사용자 정보 추출
        Claims claims = tokenProvider.validateAndGetClaims(accessToken);
        String email = claims.getSubject();

        // Redis에서 리프레시 토큰 삭제
        redisService.deleteRefreshToken(email);
        
        // 해당 액세스 토큰을 블랙리스트에 추가 (로그아웃 처리)
        long expiration = tokenProvider.getExpirationTime(accessToken);
        redisService.addToBlacklist(accessToken, expiration);
    }
}