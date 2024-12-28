package com.doosan.christmas.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.doosan.christmas.common.dto.ResponseDto;

@RestController
public class HomeController {
    
    @GetMapping("/test")
    public ResponseDto<String> home() {
        return ResponseDto.success("서버 분리 후 홈 컨트롤러 동작 성공");
    }
} 