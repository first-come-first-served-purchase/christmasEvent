package com.doosan.christmas.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDto<T> {
    private boolean success;
    private T data;
    private String message;

    public static <T> ResponseDto<T> success(T data) {
        return ResponseDto.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ResponseDto<T> success(T data, String message) {
        return ResponseDto.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    public static <T> ResponseDto<T> fail(String message) {
        return ResponseDto.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
} 