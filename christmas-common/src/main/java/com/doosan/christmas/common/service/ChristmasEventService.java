package com.doosan.christmas.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.doosan.christmas.common.repository.MenuRepository;
import com.doosan.christmas.common.repository.OrderRepository;
import com.doosan.christmas.common.domain.Order;
import com.doosan.christmas.common.domain.DiscountResult;
import lombok.RequiredArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChristmasEventService {
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    
    public DiscountResult calculateDiscount(Order order) {
        // 주문 금액 계산
        int totalAmount = calculateTotalAmount(order);
        
        // 할인 금액 계산
        int discountAmount = calculateDiscountAmount(order);
        
        // 최종 금액 계산
        int finalAmount = totalAmount - discountAmount;
        
        // 적용된 이벤트 목록
        List<String> appliedEvents = new ArrayList<>();
        
        return DiscountResult.builder()
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .appliedEvents(appliedEvents)
                .build();
    }
    
    private int calculateTotalAmount(Order order) {
        return order.getOrderItems().stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
    
    private int calculateDiscountAmount(Order order) {
        // 할인 로직 구현
        return 0; // 임시 반환값
    }
} 