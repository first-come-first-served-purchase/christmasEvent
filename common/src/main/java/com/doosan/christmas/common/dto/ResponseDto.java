package com.doosan.christmas.common.dto;

import com.doosan.christmas.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseDto<T> {
    private boolean success;
    private T data;
    private String code;
    private String message;

    public static <T> ResponseDto<T> success(T data) {
        return new ResponseDto<>(true, data, null, null);
    }

    public static <T> ResponseDto<T> fail(String code, String message) {
        return new ResponseDto<>(false, null, code, message);
    }

    public static <T> ResponseDto<T> error(ErrorCode errorCode) {
        return new ResponseDto<>(false, null, errorCode.getCode(), errorCode.getMessage());
    }
} 