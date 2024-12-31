package com.doosan.christmas.order.client.fallback;

import com.doosan.christmas.order.client.UserServiceClient;
import com.doosan.christmas.order.client.dto.UserResponse;
import com.doosan.christmas.order.exception.OrderException;
import org.springframework.stereotype.Component;

@Component
public class UserServiceFallback implements UserServiceClient {
    
    @Override
    public UserResponse getUserById(Long userId) {
        throw new OrderException.UserServiceException("사용자 서비스가 일시적으로 사용 불가능합니다.");
    }
} 