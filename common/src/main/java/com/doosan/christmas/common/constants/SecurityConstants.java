package com.doosan.christmas.common.constants;

public class SecurityConstants {
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    
    private SecurityConstants() {
        throw new IllegalStateException("Utility class");
    }
} 