package com.doosan.christmas.api.controller.member;

import com.doosan.christmas.api.dto.member.LoginRequestDto;
import com.doosan.christmas.api.dto.member.LoginResponseDto;
import com.doosan.christmas.api.dto.member.SignupRequestDto;
import com.doosan.christmas.api.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody SignupRequestDto request) {
        authenticationService.signup(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
        LoginResponseDto response = authenticationService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }
} 