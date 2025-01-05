package com.doosan.common.exception;

public class BusinessRuntimeException extends BusinessException {

    private Object additionalInfo;

    public BusinessRuntimeException(String errorMessage) {
        super(errorMessage);
    }

    public BusinessRuntimeException(Throwable cause) {
        super(cause);
    }

    public BusinessRuntimeException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public BusinessRuntimeException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }

    public BusinessRuntimeException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }
}
