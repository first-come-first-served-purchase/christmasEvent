package com.doosan.christmas.order.client.fallback;

import com.doosan.christmas.order.client.ProductServiceClient;
import com.doosan.christmas.order.client.dto.ProductResponse;
import com.doosan.christmas.order.exception.OrderException;
import org.springframework.stereotype.Component;

@Component
public class ProductServiceFallback implements ProductServiceClient {
    
    @Override
    public ProductResponse getProductById(Long productId) {
        throw new OrderException.ProductServiceException("상품 서비스가 일시적으로 사용 불가능합니다.");
    }
    
    @Override
    public void restoreStock(Long productId, Integer quantity) {
        throw new OrderException.ProductServiceException("재고 복구 처리가 실패했습니다.");
    }
} 