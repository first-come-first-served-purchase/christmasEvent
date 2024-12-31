package com.doosan.christmas.order.exception;

public class OrderException extends RuntimeException {
    public OrderException(String message) {
        super(message);
    }

    public OrderException(String message, Throwable cause) {
        super(message, cause);
    }

    // 상품 서비스 관련 예외
    public static class ProductServiceException extends OrderException {
        public ProductServiceException(String message) {
            super(message);
        }
        
        public ProductServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // 주문 상태 변경 예외
    public static class InvalidOrderStatusException extends OrderException {
        public InvalidOrderStatusException(String message) {
            super(message);
        }
    }

    // 주문 취소 실패 예외
    public static class OrderCancellationException extends OrderException {
        public OrderCancellationException(String message) {
            super(message);
        }
    }

    // 사용자 서비스 관련 예외
    public static class UserServiceException extends OrderException {
        public UserServiceException(String message) {
            super(message);
        }
        
        public UserServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
} 