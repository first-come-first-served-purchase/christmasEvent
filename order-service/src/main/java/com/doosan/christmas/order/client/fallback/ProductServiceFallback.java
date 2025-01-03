package com.doosan.christmas.order.client.fallback;

import com.doosan.christmas.order.client.ProductServiceClient;
import com.doosan.christmas.order.client.ProductResponseWrapper;
import com.doosan.christmas.order.exception.OrderException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductServiceFallback implements ProductServiceClient {

    @Override
    public ProductResponseWrapper getProductById(Long productId) {
        log.error("[ProductServiceFallback] 상품 조회 실패 - 상품 ID: {}", productId);

        // Fallback으로 실패한 결과를 반환
        ProductResponseWrapper responseWrapper = new ProductResponseWrapper();
        responseWrapper.setSuccess(false);
        responseWrapper.setError("상품 서비스가 일시적으로 사용 불가능합니다.");
        responseWrapper.setMessage("상품 정보를 조회할 수 없습니다.");
        return responseWrapper;
    }

    @Override
    public void restoreStock(Long productId, Integer quantity) {
        log.error("[ProductServiceFallback] 재고 복구 실패 - 상품 ID: {}, 복구 수량: {}", productId, quantity);
        throw new OrderException.ProductServiceException("재고 복구 처리가 실패했습니다.");
    }
}
