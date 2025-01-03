package com.doosan.christmas.product.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    
    // 상품 관련 에러
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_001", "상품을 찾을 수 없습니다."),
    PRODUCT_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "PRODUCT_002", "상품의 재고가 부족합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
} 