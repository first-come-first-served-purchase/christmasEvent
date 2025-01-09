package com.doosan.orderservice.service.impl;

import com.doosan.orderservice.entity.PaymentStatus;
import com.doosan.orderservice.entity.PaymentHistory;
import com.doosan.orderservice.event.OrderItemEvent;
import com.doosan.orderservice.repository.PaymentHistoryRepository;
import com.doosan.orderservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentHistoryRepository paymentHistoryRepository;

    // 결제 정산
    @Override
    @Transactional
    public void processPaymentSettlement(Long orderId, Long totalAmount, Long userId, List<OrderItemEvent> items) {
        try {
            PaymentHistory settlement = PaymentHistory.builder()
                    .orderId(orderId) // 주문 ID
                    .amount(totalAmount) // 결제 총액
                    .userId(userId) // 사용자 Id
                    .status(PaymentStatus.PAYMENT_COMPLETED) // 결제 상태 완료
                    .processedAt(new Date()) // 처리 시간
                    .build();
            
            paymentHistoryRepository.save(settlement); // 결제 기록 저장
            log.info("결제 정산 처리 완료 - 주문 ID: {}, 총액: {}, 사용자 ID: {}", orderId, totalAmount, userId);

        } catch (Exception e) {
            log.error("결제 정산 처리 실패 - 주문 ID: {}", orderId, e);
            throw new RuntimeException("결제 정산 처리 중 오류 발생", e);
        }
    }

    // 결제 실패 기록
    @Override
    @Transactional
    public void recordPaymentFailure(Long orderId, Long userId, PaymentStatus status) {
        try {
            PaymentHistory failureRecord = PaymentHistory.builder()
                    .orderId(orderId) // 주문 iD
                    .userId(userId) // 사용자 ID
                    .status(status) // 결제 상태
                    .processedAt(new Date()) // 처리시간
                    .build();

            paymentHistoryRepository.save(failureRecord); // 결제 실패 기록 저장
            log.info("결제 실패 기록 완료 - 주문 ID: {}, 사용자 ID: {}", orderId, userId);

        } catch (Exception e) {
            log.error("결제 실패 기록 실패 - 주문 ID: {}", orderId, e);

            throw new RuntimeException("결제 실패 기록 중 오류 발생", e);
        }
    }

    // 환불 처리
    @Override
    @Transactional
    public void processRefund(Long orderId, Long amount) {
        try {
            PaymentHistory refund = PaymentHistory.builder()
                    .orderId(orderId) // 주문 iD
                    .amount(amount) // 환불 금액
                    .status(PaymentStatus.REFUNDED)  // 상태 환불
                    .processedAt(new Date()) // 처리 시간
                    .build();
            
            paymentHistoryRepository.save(refund); // 환불 기록 저장
            log.info("환불 처리 완료 - 주문 ID: {}, 환불액: {}", orderId, amount);

        } catch (Exception e) {
            log.error("환불 처리 실패 - 주문 ID: {}", orderId, e);

            throw new RuntimeException("환불 처리 중 오류 발생", e);
        }
    }

    // 결제 처리
    @Transactional
    @Override
    public void processPayment(Long orderId, Long totalAmount, Long userId) {

        try {
            PaymentHistory settlement = PaymentHistory.builder()
                    .orderId(orderId) // 주문 ID
                    .amount(totalAmount) // 결제 총액
                    .userId(userId) // 사용자 ID
                    .status(PaymentStatus.PAYMENT_COMPLETED) // 결제 완료 상태 설정
                    .processedAt(new Date()) // 처리 시간
                    .build();


            paymentHistoryRepository.save(settlement);//  결제 기록 저장
            log.info("결제 정산 처리 완료 - 주문 ID: {}, 총액: {}, 사용자 ID: {}", orderId, totalAmount, userId);

        } catch (Exception e) {
            log.error("결제 정산 처리 실패 - 주문 ID: {}", orderId, e);
            throw new RuntimeException("결제 정산 처리 중 오류 발생", e);
        }
    }
} 