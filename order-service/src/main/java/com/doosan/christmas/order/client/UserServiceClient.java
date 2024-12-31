package com.doosan.christmas.order.client;

import com.doosan.christmas.order.client.dto.UserResponse;
import com.doosan.christmas.order.client.fallback.UserServiceFallback;
import com.doosan.christmas.order.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "user-service",
    fallback = UserServiceFallback.class,
    configuration = FeignClientConfig.class
)
public interface UserServiceClient {
    @GetMapping("/api/v1/users/{userId}")
    UserResponse getUserById(@PathVariable("userId") Long userId);
} 