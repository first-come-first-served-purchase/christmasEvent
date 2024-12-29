package com.doosan.christmas.user.controller;

import com.doosan.christmas.common.dto.ResponseDto;
import com.doosan.christmas.common.exception.CustomException;
import com.doosan.christmas.common.exception.ErrorCode;
import com.doosan.christmas.user.domain.Member;
import com.doosan.christmas.user.dto.EmailRequest;
import com.doosan.christmas.user.dto.SignupRequest;
import com.doosan.christmas.user.dto.VerifyEmailRequest;
import com.doosan.christmas.user.service.AuthService;
import com.doosan.christmas.user.security.TokenProvider;
import com.doosan.christmas.user.dto.TokenDto;
import com.doosan.christmas.user.dto.LoginRequest;
import com.doosan.christmas.user.service.RedisService;
import com.doosan.christmas.user.dto.TokenRefreshRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final TokenProvider tokenProvider;
    private final RedisService redisService;
    
    @PostMapping("/signup")
    public ResponseDto<Void> signup(@Valid @RequestBody SignupRequest request) {
        log.debug("Received signup request for email: {}", request.getEmail());
        Member member = request.toEntity();
        authService.signup(member);
        return ResponseDto.success(null);
    }
    
    @PostMapping("/email")
    public ResponseDto<Void> sendVerificationEmail(@RequestBody @Valid EmailRequest request) {
        log.debug("Received email verification request for: {}", request.getEmail());
        authService.sendVerificationEmail(request.getEmail());
        return ResponseDto.success(null);
    }

    @PostMapping("/verify-code")
    public ResponseDto<Void> verifyEmail(@RequestBody @Valid VerifyEmailRequest request) {
        log.debug("Received verification code check request - Email: {}", request.getEmail());
        authService.verifyEmail(request.getEmail(), request.getCode());
        return ResponseDto.success(null);
    }

    @PostMapping("/login")
    public ResponseDto<TokenDto> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Received login request for email: {}", request.getEmail());
        
        Member member = authService.login(request.getEmail(), request.getPassword());
        TokenDto token = tokenProvider.generateToken(member);
        
        redisService.saveRefreshToken(member.getEmail(), token.getRefresh_token());
        
        return ResponseDto.success(token);
    }

    @PostMapping("/refresh")
    public ResponseDto<TokenDto> refreshToken(@RequestBody TokenRefreshRequest request) {
        log.info("Token refresh requested with refresh token");
        
        if (request == null || request.getRefresh_token() == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        
        try {
            TokenDto newToken = authService.refreshToken(request.getRefresh_token());
            log.info("Token refresh successful");
            return ResponseDto.success(newToken);
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/logout")
    public ResponseDto<String> logout(@RequestHeader("Authorization") String token) {
        log.info("Logout requested with token: {}", token);
        
        if (token == null || !token.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        
        try {
            authService.logout(token.replace("Bearer ", ""));
            log.info("Logout successful");
            return ResponseDto.success("로그아웃이 성공적으로 처리되었습니다.");
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            throw e;
        }
    }
} 