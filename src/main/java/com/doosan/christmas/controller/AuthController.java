package com.doosan.christmas.controller;

import com.doosan.christmas.dto.requestdto.EmailAuthRequestDTO;
import com.doosan.christmas.dto.responsedto.ResponseDto;
import com.doosan.christmas.service.MemberService;
import com.doosan.christmas.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final MemberService memberService;
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    /**
     * 이메일로 인증 코드 발송
     */
    @PostMapping("/email")
    public ResponseDto<?> sendEmailAuth(@RequestBody EmailAuthRequestDTO requestDTO) {
        logger.info("이메일 인증 코드 발송 요청 - 이메일: {}", requestDTO.getEmail());
        return memberService.sendEmailAuth(requestDTO);
    }

    /**
     * 인증 코드 확인
     */
    @PostMapping("/verify-code")
    public ResponseDto<?> verifyEmailCode(@RequestBody EmailAuthRequestDTO request) {
        logger.info("인증 코드 검증 요청: {}", request);
        
        if (request.getEmail() == null || request.getAuthCode() == null) {
            return ResponseDto.fail("INVALID_INPUT", "이메일과 인증 코드는 필수 입력 항목입니다.");
        }

        return authService.verifyEmailCode(request.getEmail(), request.getAuthCode());
    }
}
