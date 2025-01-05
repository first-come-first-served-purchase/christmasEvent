package com.doosan.common.utils;

import jakarta.servlet.http.HttpServletRequest;

public class ParseRequestUtil {
    private final JwtUtil jwtUtil;

    public ParseRequestUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public int extractUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("인증 토큰이 없거나 잘못된 형식입니다.");
        }

        String token = authHeader.substring(7);
        return jwtUtil.getUserIdFromToken(token);
    }
}
