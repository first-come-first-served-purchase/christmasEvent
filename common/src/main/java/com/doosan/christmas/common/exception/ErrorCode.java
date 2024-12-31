package com.doosan.christmas.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    INVALID_INPUT_VALUE("400", "Invalid input value"),
    INTERNAL_SERVER_ERROR("500", "Internal server error"),
    PRODUCT_NOT_FOUND("500", "product not found");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
