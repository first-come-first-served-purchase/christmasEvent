package com.doosan.orderservice.controller;

import com.doosan.common.dto.ResponseDto;
import com.doosan.orderservice.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
@Log4j2
public class StockController {
    private final StockService stockService;

    // 재고 초기화
    @PostMapping("/initialize/{productId}")
    public ResponseEntity<ResponseDto<Void>> initializeStock(@PathVariable Long productId) {
        try {
            stockService.initializeStock(productId);
            return ResponseEntity.ok(
                ResponseDto.<Void>builder()
                    .statusCode(HttpStatus.OK.value())
                    .resultMessage("재고 초기화 완료")
                    .build()
            );
        } catch (Exception e) {
            log.error("재고 초기화 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.<Void>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .resultMessage("재고 초기화 실패: " + e.getMessage())
                    .build()
                );
        }
    }

    // 재고 모니터링
    @GetMapping("/monitor/{productId}")
    public ResponseEntity<ResponseDto<List<String>>> getStockHistory(@PathVariable Long productId) {
        try {
            List<String> history = stockService.getStockMonitoringHistory(productId);
            return ResponseEntity.ok(
                ResponseDto.<List<String>>builder()
                    .statusCode(HttpStatus.OK.value())
                    .resultMessage("재고 모니터링 조회 완료")
                    .data(history)
                    .build()
            );
        } catch (Exception e) {
            log.error("재고 모니터링 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.<List<String>>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .resultMessage("재고 모니터링 조회 실패: " + e.getMessage())
                    .build()
                );
        }
    }
} 