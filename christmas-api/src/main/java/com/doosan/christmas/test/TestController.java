package com.doosan.christmas.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.doosan.christmas.common.dto.ResponseDto;

@RestController
public class TestController {
    
    @GetMapping("/api/test")
    public ResponseDto<String> test() {
        return ResponseDto.success("Test API 동작 성공");
    }
} 