package com.doosan.christmas.product.dto.responseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter // 모든 필드의 getter 자동 생성
@AllArgsConstructor // 모든 필드를 초기화하는 생성자 자동 생성
public class ResponseDto<T> {
    private boolean success; // 요청 성공 여부
    private T data; // 성공 시 반환 데이터
    private String error; // 오류 코드
    private String message; // 오류 메시지

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