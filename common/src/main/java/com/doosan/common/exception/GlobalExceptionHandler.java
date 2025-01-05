package com.doosan.common.exception;


import com.doosan.common.dto.ResponseMessage;
import com.doosan.common.enums.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({BusinessRuntimeException.class})
    @ResponseBody
    public ResponseEntity<ResponseMessage> handleBizRuntimeException(BusinessRuntimeException e) {
        log.error("BizRuntime Error: " + e.getErrorMessage(), e);

        ResponseMessage responseMessage = ResponseMessage.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .resultMessage(ApiResponse.ERROR + " " + e.getErrorMessage())
                .detailMessage(e.getMessage())
                .build();

        return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ResponseEntity<ResponseMessage> handleGeneralException(Exception e, HttpServletRequest request, HttpServletResponse response) {
        log.error("Unexpected Error: " + e.getMessage(), e);

        String msgId = "";
        String msg = e.getMessage();

        if (e instanceof BusinessException) {
            BusinessException base = (BusinessException) e;
            msgId = base.getErrorCode();
            msg = base.getMessage();
        }

        ResponseMessage responseMessage = ResponseMessage.builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .resultMessage("서버 내부 오류가 발생했습니다.")
                .detailMessage(msg)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMessage);
    }
}
