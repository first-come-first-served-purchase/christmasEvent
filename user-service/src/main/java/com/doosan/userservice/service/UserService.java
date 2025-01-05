package com.doosan.userservice.service;

import com.doosan.common.dto.ResponseMessage;
import com.doosan.userservice.dto.EmailAuthRequestDto;
import com.doosan.userservice.dto.LoginRequestDto;
import com.doosan.userservice.dto.PasswordChangeRequestDto;
import com.doosan.userservice.dto.SignupRequestDto;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<ResponseMessage> signup(SignupRequestDto createUserRequestDto);

    ResponseEntity<ResponseMessage> login(LoginRequestDto loginRequestDto);

    ResponseEntity<ResponseMessage> logout(String token);

    ResponseEntity<ResponseMessage> changePassword(String token, PasswordChangeRequestDto passwordChangeRequestDto);

    ResponseEntity<ResponseMessage> sendEmailAuth(EmailAuthRequestDto requestDto);

    ResponseEntity<ResponseMessage> verifyEmailCode(EmailAuthRequestDto requestDTO);

    ResponseEntity<ResponseMessage> reissueToken(String accessToken, String refreshToken);


}