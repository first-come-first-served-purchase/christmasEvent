package com.doosan.christmas.order.controller;

import com.doosan.christmas.member.domain.UserDetailsImpl;
import com.doosan.christmas.order.domain.OrderHistoryResponse;
import com.doosan.christmas.order.domain.OrderSearchCondition;
import com.doosan.christmas.order.dto.requestDto.OrderRequestDto;
import com.doosan.christmas.order.dto.responseDto.OrderResponseDto;
import com.doosan.christmas.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "주문 API", description = "주문 관련 API")
public class OrderController {

    private final OrderService orderService;

    /**
     * 어드민 수동 배송완료 변경 api , ROLE_ADMIN 권한만 접근 가능
     *
     * @param orderId  주문 ID
     * @param memberId 회원 ID
     * @return 강제로 배송 완료 상태로 변경된 주문 정보 (OrderResponseDto)
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{orderId}/force-complete-delivery")
    public ResponseEntity<OrderResponseDto> forceCompleteDelivery(@PathVariable Long orderId, @RequestParam Long memberId) {

        try {

            OrderResponseDto orderResponseDto = orderService.forceCompleteDelivery(orderId, memberId);

            return ResponseEntity.ok(orderResponseDto);

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 주문 생성
     *
     * @param request     주문 요청 DTO
     * @param userDetails 현재 로그인 한 사용자 정보
     * @return 생성된 주문 정보 (OrderResponseDto)
     */
    @PostMapping
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성 합니다.")
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody @Valid OrderRequestDto request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(
                orderService.createOrder(request, userDetails.getMember().getId())
        );
    }

    /**
     * 주문 취소
     *
     * @param orderId 주문 ID
     * @param userDetails 현재 로그인 한 사용자 정보
     * @return 취소된 주문 정보 (OrderResponseDto)
     */
    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "주문 취소", description = "주문을 취소 합니다.")
    public ResponseEntity<OrderResponseDto> cancelOrder(@PathVariable Long orderId,@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(
                orderService.cancelOrder(orderId, userDetails.getMember().getId())
        );
    }

    /**
     * 주문 반품
     *
     * @param orderId 주문 ID
     * @param userDetails 현재 로그인 한 사용자 정보
     * @return 반품된 주문 정보 (OrderResponseDto)
     */
    @PostMapping("/{orderId}/return")
    @Operation(summary = "주문 반품", description = "주문을 반품 합니다.")
    public ResponseEntity<OrderResponseDto> returnOrder( @PathVariable Long orderId,@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(
                orderService.returnOrder(orderId, userDetails.getMember().getId())
        );
    }

    /**
     * 주문 이력 조회
     *
     * @param condition 검색 조건 (OrderSearchCondition)
     * @param pageable 페이징 정보 (Pageable)
     * @param userDetails 현재 로그인 한 사용자 정보
     * @return 주문 이력 (Page<OrderHistoryResponse>)
     */
    @GetMapping
    @Operation(summary = "주문 이력 조회", description = "사용자의 주문 이력을 조회합니다.")
    public ResponseEntity<Page<OrderHistoryResponse>> getOrderHistory( @ModelAttribute OrderSearchCondition condition,@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(
                orderService.getOrderHistory(userDetails.getMember().getId(), condition, pageable)
        );
    }

    /**
     * 주문 상세 조회
     *
     * @param orderId 주문 ID
     * @param userDetails 현재 로그인 한 사용자 정보
     * @return 주문 상세 정보 (OrderHistoryResponse)
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "주문 상세 조회", description = "특정 주문의 상세 정보를 조회합니다.")
    public ResponseEntity<OrderHistoryResponse> getOrderDetail( @PathVariable Long orderId,@AuthenticationPrincipal UserDetailsImpl userDetails) {

        // userDetails가 null이거나 userDetails.getMember()가 null인 경우를 처리
        if (userDetails == null || userDetails.getMember() == null) {

            // 인증되지 않은 사용자에 대한 접근 제어
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        return ResponseEntity.ok(
                orderService.getOrderDetail(orderId, userDetails.getMember().getId())
        );
    }
} 