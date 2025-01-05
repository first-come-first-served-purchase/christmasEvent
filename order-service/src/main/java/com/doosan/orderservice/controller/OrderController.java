package com.doosan.orderservice.controller;


import com.doosan.common.dto.ResponseMessage;
import com.doosan.common.dto.order.CreateOrderReqDto;
import com.doosan.common.utils.ParseRequestUtil;
import com.doosan.orderservice.dto.CreateOrderResDto;
import com.doosan.orderservice.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("create-order")
    public ResponseEntity<ResponseMessage> createOrder(HttpServletRequest request, @RequestBody List<CreateOrderReqDto> orderItems) {
        int userId = new ParseRequestUtil().extractUserIdFromRequest(request);

        CreateOrderResDto orderResponse = orderService.createOrder(userId, orderItems);

        ResponseMessage response = ResponseMessage.builder()
                .data(orderResponse)
                .statusCode(200)
                .resultMessage("Success")
                .build();

        return ResponseEntity.ok(response);
    }
}
