package com.doosan.orderservice.service;

import com.doosan.orderservice.entity.PaymentHistory;
import com.doosan.orderservice.entity.PaymentStatus;
import com.doosan.orderservice.event.OrderItemEvent;
import com.doosan.orderservice.repository.PaymentHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class ReactivePaymentService {
    private final PaymentHistoryRepository paymentHistoryRepository;

    // 결제 정산 처리
    public Mono<Void> processPaymentSettlement(Long orderId, Long amount, Long userId, List<OrderItemEvent> items) {
        PaymentHistory paymentHistory = PaymentHistory.builder()
                .orderId(orderId)
                .userId(userId)
                .amount(amount)
                .status(PaymentStatus.PAYMENT_COMPLETED)
                .processedAt(new Date())
                .build();

        return Mono.fromCallable(() -> paymentHistoryRepository.save(paymentHistory))
                .doOnSuccess(v -> log.info("결제 정산 처리 완료 - 주문: {}", orderId))
                .doOnError(e -> log.error("결제 정산 처리 실패", e))
                .then();
    }

    // 결제 취소
    public Mono<Void> cancelPayment(Long orderId) {
        return Mono.fromCallable(() -> {
                    PaymentHistory paymentHistory = PaymentHistory.builder()
                            .orderId(orderId)
                            .status(PaymentStatus.CANCELLED)
                            .processedAt(new Date())
                            .build();
                    return paymentHistoryRepository.save(paymentHistory);
                })
                .doOnSuccess(v -> log.info("결제 취소 처리 완료 - 주문: {}", orderId))
                .doOnError(e -> log.error("결제 취소 처리 실패", e))
                .then();
    }

    // 결제 실패 기록
    public Mono<Void> recordPaymentFailure(Long orderId, Long userId, PaymentStatus status) {
        PaymentHistory failureRecord = PaymentHistory.builder()
                .orderId(orderId)
                .userId(userId)
                .status(status)
                .processedAt(new Date())
                .build();

        return Mono.fromCallable(() -> paymentHistoryRepository.save(failureRecord))
                .doOnSuccess(v -> log.info("결제 실패 기록 완료 - 주문: {}, 사용자: {}", orderId, userId))
                .doOnError(e -> log.error("결제 실패 기록 실패 - 주문: {}", orderId, e))
                .then();
    }

    // 환불
    public Mono<Void> processRefund(Long orderId, Long amount) {
        PaymentHistory refund = PaymentHistory.builder()
                .orderId(orderId)
                .amount(amount)
                .status(PaymentStatus.REFUNDED)
                .processedAt(new Date())
                .build();

        return Mono.fromCallable(() -> paymentHistoryRepository.save(refund))
                .doOnSuccess(v -> log.info("환불 처리 완료 - 주문: {}, 환불액: {}", orderId, amount))
                .doOnError(e -> log.error("환불 처리 실패 - 주문: {}", orderId, e))
                .then();
    }
}