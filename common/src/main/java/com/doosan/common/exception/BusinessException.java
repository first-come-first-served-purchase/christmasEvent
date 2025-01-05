package com.doosan.common.exception;

public class BusinessException extends RuntimeException {

    private String errorCode;
    private String errorMessage;

    public BusinessException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }

    public BusinessException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    };

    public BusinessException(String errorCode, String errorMessage, Throwable cause) {
        super(cause.getMessage(), cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    };

    public BusinessException(String errorMessage, Throwable cause) {
        super(cause.getMessage(), cause);
        this.errorMessage = errorMessage;
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }

    public String getErrorCode(){
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
