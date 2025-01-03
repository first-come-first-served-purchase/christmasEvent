package com.doosan.christmas.order.client.fallback;

import com.doosan.christmas.order.client.UserServiceClient;
import com.doosan.christmas.order.client.dto.UserResponse;
import com.doosan.christmas.order.exception.OrderException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserServiceFallback implements UserServiceClient {
    
    @Override
    public ResponseEntity<UserResponse> getUserById(Long userId) {
        log.error("[UserServiceFallback] 사용자 서비스 호출 실패 - 사용자 ID: {}", userId);
        throw new OrderException.UserServiceException("사용자 서비스가 일시적으로 사용 불가능합니다.");
    }
} 