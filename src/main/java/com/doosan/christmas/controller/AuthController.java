package com.doosan.christmas.controller;

import com.doosan.christmas.dto.requestdto.EmailAuthRequestDTO;
import com.doosan.christmas.dto.responsedto.ResponseDto;
import com.doosan.christmas.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final MemberService memberService;

    /**
     * 이메일로 인증 코드 발송
     */
    @PostMapping("/email")
    public ResponseDto<?> sendEmailAuth(@RequestBody EmailAuthRequestDTO requestDTO) {
        log.info("이메일 인증 코드 발송 요청 - 이메일: {}", requestDTO.getEmail());
        return memberService.sendEmailAuth(requestDTO);
    }

    /**
     * 인증 코드 확인
     */
    @PostMapping("/verify-code")
    public ResponseDto<?> verifyEmailCode(@RequestBody EmailAuthRequestDTO requestDTO) {
        log.info("인증 코드 검증 요청: {}", requestDTO);
        return memberService.verifyEmailCode(requestDTO);
    }
}
