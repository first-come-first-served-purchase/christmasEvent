package com.doosan.userservice.controller;

import com.doosan.common.dto.ResponseMessage;
import com.doosan.userservice.dto.LoginRequestDto;
import com.doosan.userservice.dto.PasswordChangeRequestDto;
import com.doosan.userservice.dto.SignupRequestDto;
import com.doosan.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ResponseMessage> createUser(@RequestBody SignupRequestDto signupRequestDto) {
        return userService.signup(signupRequestDto);
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ResponseMessage> login(@RequestBody LoginRequestDto loginRequestDto) {
        return userService.login(loginRequestDto);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ResponseMessage> logout(@RequestHeader("Authorization") String token) {
        return userService.logout(token);
    }

    // 비밀번호 변경 후 모든 기기 로그아웃
    @PutMapping("/password")
    public ResponseEntity<ResponseMessage> changePassword(@RequestHeader("Authorization") String token,@RequestBody PasswordChangeRequestDto passwordChangeRequestDto) {
        return userService.changePassword(token, passwordChangeRequestDto);
    }

    // 토큰 재발급
    @PostMapping("/reissue")
    public ResponseEntity<ResponseMessage> reissueToken(@RequestHeader("Authorization") String accessToken, @RequestHeader("RefreshToken") String refreshToken) {
        return userService.reissueToken(accessToken, refreshToken);
    }

}