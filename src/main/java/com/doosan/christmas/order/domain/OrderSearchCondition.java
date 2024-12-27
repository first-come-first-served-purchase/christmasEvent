package com.doosan.christmas.order.domain;

import com.doosan.christmas.order.shared.OrderStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class OrderSearchCondition {

    // 주문 상태 필터링을 위한 변수
    private OrderStatus status;

    // 시작 날짜 필터링을 위한 변수
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;

    // 종료 날짜 필터링을 위한 변수
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;
}

