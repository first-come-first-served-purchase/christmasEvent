package com.doosan.christmas.order.client;

import com.doosan.christmas.order.client.dto.ProductResponse;
import com.doosan.christmas.order.client.fallback.ProductServiceFallback;
import com.doosan.christmas.order.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(
    name = "product-service",
    fallback = ProductServiceFallback.class,
    configuration = FeignClientConfig.class
)
public interface ProductServiceClient {
    @GetMapping("/api/v1/products/{productId}")
    ProductResponse getProductById(@PathVariable("productId") Long productId);
    
    @PutMapping("/api/v1/products/{productId}/restore-stock/{quantity}")
    void restoreStock(@PathVariable("productId") Long productId, 
                     @PathVariable("quantity") Integer quantity);
} 