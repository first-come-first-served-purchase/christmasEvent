package com.doosan.christmas.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@Component("commonGlobalExceptionHandler")
public class GlobalExceptionHandler {
}