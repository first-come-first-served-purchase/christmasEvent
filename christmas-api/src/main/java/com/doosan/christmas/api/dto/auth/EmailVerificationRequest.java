package com.doosan.christmas.api.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailVerificationRequest {
    private String email;
    private String verificationCode;
} 