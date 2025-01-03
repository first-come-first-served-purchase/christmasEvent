package com.doosan.christmas.order.exception;

public class OrderException extends RuntimeException {
    public OrderException(String message) {
        super(message);
    }

    public OrderException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class OrderNotFoundException extends OrderException {
        public OrderNotFoundException(String message) {
            super(message);
        }
    }

    public static class ServiceUnavailableException extends OrderException {
        public ServiceUnavailableException(String message) {
            super(message);
        }
    }

    public static class UserServiceException extends OrderException {
        public UserServiceException(String message) {
            super(message);
        }
    }

    public static class ProductServiceException extends OrderException {
        public ProductServiceException(String message) {
            super(message);
        }
    }

    public static class InvalidOrderStatusException extends OrderException {
        public InvalidOrderStatusException(String message) {
            super(message);
        }
    }

    public static class OrderCancellationException extends OrderException {
        public OrderCancellationException(String message) {
            super(message);
        }
    }
} 