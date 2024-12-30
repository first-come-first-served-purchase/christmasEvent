package com.doosan.christmas.product.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseDto<T> {
    private boolean success;
    private T data;
    private String error;
    private String message;

    /**
     * 성공 응답 생성
     */
    public static <T> ResponseDto<T> success(T data) {
        return new ResponseDto<>(true, data, null, null);
    }

    /**
     * 실패 응답 생성
     */
    public static ResponseDto<?> fail(String error, String message) {
        return new ResponseDto<>(false, null, error, message);
    }
} 