package com.doosan.christmas.dto.responsedto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseDto<T> {
    private boolean success; // 요청 성공 여부
    private T data; // 성공 시 반환 데이터
    private Error error; // 실패 시 반환할 에러 정보

    // 성공 응답 생성 메서드
    public static <T> ResponseDto<T> success(T data) {
        return new ResponseDto<>(true, data, null);
    }

    // 실패 응답 생성 메서드
    public static <T> ResponseDto<T> fail(String code, String message) {
        return new ResponseDto<>(false, null, new Error(code, message));
    }

    @Getter
    @AllArgsConstructor // 에러 코드와 메시지 포함한 생성자
    static class Error {
        private String code; // 에러 코드
        private String message; // 에러 메시지
    }
}