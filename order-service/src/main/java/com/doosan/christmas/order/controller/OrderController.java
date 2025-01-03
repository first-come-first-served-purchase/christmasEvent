package com.doosan.christmas.order.controller;

import com.doosan.christmas.common.dto.ResponseDto;
import com.doosan.christmas.order.domain.OrderHistoryResponse;
import com.doosan.christmas.order.dto.requestDto.OrderRequestDto;
import com.doosan.christmas.order.dto.responseDto.OrderResponseDto;
import com.doosan.christmas.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "주문 API", description = "주문 관련 API")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다.")
    public ResponseEntity<ResponseDto<List<OrderResponseDto>>> createOrders(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestBody List<OrderRequestDto> requestDtos) {
        log.info("주문 생성 요청 - 사용자 ID: {}, 요청 데이터 수: {}", userId, requestDtos.size());

        try {
            List<OrderResponseDto> responses = orderService.createOrders(userId, requestDtos);
            log.info("✅ [주문 생성 성공] 사용자 ID: {}, 총 주문 수: {}", userId, responses.size());
            return ResponseEntity.ok(ResponseDto.success(responses));
        } catch (Exception e) {
            log.error("❌ [주문 생성 실패] 사용자 ID: {}, 요청 데이터: {}, 에러 메시지: {}",
                    userId, requestDtos, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.fail("ORDER_CREATE_ERROR", e.getMessage()));
        }
    }


    @DeleteMapping("/{orderId}")
    @Operation(summary = "주문 취소", description = "주문을 취소합니다.")
    public ResponseEntity<ResponseDto<Void>> cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader("X-USER-ID") Long userId) {
        log.info("❌ [주문 취소 요청] 사용자 ID: {}, 주문 ID: {}", userId, orderId);
        try {
            orderService.cancelOrder(orderId, userId);
            log.info("✅ [주문 취소 성공] 사용자 ID: {}, 주문 ID: {}", userId, orderId);
            return ResponseEntity.ok(ResponseDto.success(null));
        } catch (Exception e) {
            log.error("❌ [주문 취소 실패] 사용자 ID: {}, 주문 ID: {}, 에러 메시지: {}", userId, orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.fail("ORDER_CANCEL_ERROR", e.getMessage()));
        }
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "주문 상세 조회", description = "특정 주문의 상세 정보를 조회합니다.")
    public ResponseEntity<ResponseDto<OrderHistoryResponse>> getOrderDetail(
            @PathVariable Long orderId,
            @RequestHeader("X-USER-ID") Long userId) {
        log.info("🔍 [주문 상세 조회 요청] 사용자 ID: {}, 주문 ID: {}", userId, orderId);
        try {
            OrderHistoryResponse response = orderService.getOrderDetail(orderId, userId);
            log.info("✅ [주문 상세 조회 성공] 사용자 ID: {}, 주문 ID: {}, 조회 결과: {}", userId, orderId, response);
            return ResponseEntity.ok(ResponseDto.success(response));
        } catch (Exception e) {
            log.error("❌ [주문 상세 조회 실패] 사용자 ID: {}, 주문 ID: {}, 에러 메시지: {}", userId, orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.fail("ORDER_DETAIL_ERROR", e.getMessage()));
        }
    }
}
