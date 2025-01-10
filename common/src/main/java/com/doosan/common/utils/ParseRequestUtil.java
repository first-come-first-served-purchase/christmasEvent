package com.doosan.common.utils;

import com.doosan.common.exception.BusinessRuntimeException;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ParseRequestUtil {

    private final JwtUtil jwtUtil; // JWT 유틸 클래스 주입

    // ParseRequestUtil 생성자
    public ParseRequestUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // 요청에서 사용자 ID를 추출
    public Mono<Integer> extractUserIdFromRequest(ServerHttpRequest request) {

        // HTTP 요청 헤더에서 Authorization 값을 가져옴
        return Mono.justOrEmpty(request.getHeaders().getFirst("Authorization"))
                .map(token -> {

                    // 토큰이 존재하고 BEARER_PREFIX로 시작하는 경우
                    if (token != null && token.startsWith(JwtUtil.BEARER_PREFIX)) {
                        String actualToken = token.substring(7); // BEARER_PREFIX 이후의 실제 토큰 추출

                        // 토큰 유효성 검증
                        if (jwtUtil.validateToken(actualToken)) {
                            return jwtUtil.getUserIdFromToken(actualToken); // 토큰에서 사용자 ID 추출
                        }

                        // 유효하지 않은 토큰이면 예외 발생
                        throw new BusinessRuntimeException("유효하지 않은 토큰입니다.");
                    }

                    // 토큰 형식 틀리면 예외 발생
                    throw new BusinessRuntimeException("토큰 형식이 올바르지 않습니다.");
                });
    }
}
