package com.doosan.christmas.api.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.doosan.christmas.api.service.AuthService;
import com.doosan.christmas.api.dto.auth.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody EmailVerificationRequest request) {
        authService.verifyCode(request.getEmail(), request.getVerificationCode());
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/email")
    public ResponseEntity<?> sendEmail(@RequestBody EmailRequest request) {
        authService.sendEmail(request.getEmail());
        return ResponseEntity.ok().build();
    }
} 