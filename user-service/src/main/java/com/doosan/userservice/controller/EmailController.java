package com.doosan.userservice.controller;

import com.doosan.common.dto.ResponseMessage;
import com.doosan.userservice.dto.EmailAuthRequestDto;
import com.doosan.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class EmailController {

    private final UserService userService;

    @PostMapping("/email")
    public ResponseEntity<ResponseMessage> sendVerificationEmail(@RequestParam String email) {
        EmailAuthRequestDto requestDto = new EmailAuthRequestDto();
        requestDto.setEmail(email);
        return userService.sendEmailAuth(requestDto);
    }

    @PostMapping("/verify")
    public ResponseEntity<ResponseMessage> verifyEmail( @RequestParam String email,@RequestParam String code) {
        EmailAuthRequestDto requestDto = new EmailAuthRequestDto();
        requestDto.setEmail(email);
        requestDto.setAuthCode(code);
        return userService.verifyEmailCode(requestDto);
    }
}
