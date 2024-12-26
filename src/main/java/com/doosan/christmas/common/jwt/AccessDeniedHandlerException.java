package com.doosan.christmas.common.jwt;

import com.doosan.christmas.member.dto.responsedto.ResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AccessDeniedHandlerException implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        // JSON 형태로 에러 응답 설정
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().println(
                new ObjectMapper().writeValueAsString(
                        ResponseDto.fail("BAD_REQUEST", "이 작업은 로그인이 필요합니다.") // 로그인 요구 에러 메시지
                )
        );
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 상태 코드 설정
    }
}