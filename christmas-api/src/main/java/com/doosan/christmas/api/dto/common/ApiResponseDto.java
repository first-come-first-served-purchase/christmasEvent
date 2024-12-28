package com.doosan.christmas.api.dto.common;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ApiResponseDto<T> {
    private boolean success;
    private T data;
    private ErrorDto error;

    @Getter
    @NoArgsConstructor
    public static class ErrorDto {
        private String code;
        private String message;

        public ErrorDto(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    public static <T> ApiResponseDto<T> success(T data) {
        ApiResponseDto<T> response = new ApiResponseDto<>();
        response.success = true;
        response.data = data;
        return response;
    }

    public static <T> ApiResponseDto<T> error(String code, String message) {
        ApiResponseDto<T> response = new ApiResponseDto<>();
        response.success = false;
        response.error = new ErrorDto(code, message);
        return response;
    }
} 