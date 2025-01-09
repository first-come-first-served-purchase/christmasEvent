package com.doosan.orderservice.service.impl;

import com.doosan.orderservice.entity.Notification;
import com.doosan.orderservice.repository.NotificationRepository;
import com.doosan.orderservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    // 알림 데이터를 저장하기 위한 JPA Repository
    private final NotificationRepository notificationRepository;

    // 주문 완료 알림 전송
    @Override
    @Transactional // 트랜잭션 관리, 성공 시 커밋, 실패 시 롤백
    public void sendOrderCompletionNotification(Long userId, Long orderId, Long amount) {
        try {
            // 주문 완료 알림 데이터 생성
            Notification notification = Notification.builder()
                    .userId(userId) // 사용자 ID 설정
                    .orderId(orderId) // 주문 ID 설정
                    .type("ORDER_COMPLETION") // 알림 타입 설정 (주문 완료)
                    .message(String.format("주문이 완료되었습니다. 주문번호: %d, 결제금액: %d원", orderId, amount)) // 메시지 생성
                    .sentAt(new Date()) // 알림 전송 시간 설정
                    .build();

            // 알림 데이터 저장
            notificationRepository.save(notification);
            log.info("주문 완료 알림 저장 - 사용자: {}, 주문: {}", userId, orderId);

        } catch (Exception e) {

            // 오류 발생 시 로그 출력 및 예외 전파
            log.error("주문 완료 알림 실패 - 사용자: {}, 주문: {}", userId, orderId, e);
            throw new RuntimeException("알림 처리 중 오류 발생", e);
        }
    }

    // 결제 실패 알림 전송
    @Override
    @Transactional
    public void sendPaymentFailureNotification(Long userId, Long orderId) {
        try {
            // 결제 실패 알림 데이터 생성
            Notification notification = Notification.builder()
                    .userId(userId) // 사용자 ID 설정
                    .orderId(orderId) // 주문 ID 설정
                    .type("PAYMENT_FAILURE") // 알림 타입 설정
                    .message(String.format("결제가 실패했습니다. 주문번호: %d", orderId)) // 메시지 생성
                    .sentAt(new Date()) // 알림 전송 시간 설정
                    .build();

            notificationRepository.save(notification);  // 알림 데이터 저장
            log.info("결제 실패 알림 저장 - 사용자: {}, 주문: {}", userId, orderId);

        } catch (Exception e) {

            log.error("결제 실패 알림 실패 - 사용자: {}, 주문: {}", userId, orderId, e);
            throw new RuntimeException("알림 처리 중 오류 발생", e);
        }
    }

    //주문 취소 알림 전송
    @Override
    @Transactional
    public void sendOrderCancellationNotification(Long userId, Long orderId) {
        try {
            // 주문 취소 알림 데이터 생성
            Notification notification = Notification.builder()
                    .userId(userId) // 사용자 ID 설정
                    .orderId(orderId) // 주문 ID 설정
                    .type("ORDER_CANCELLATION") // 알림 타입 설정 (주문 취소)
                    .message(String.format("주문이 취소되었습니다. 주문번호: %d", orderId)) // 메시지 생성
                    .sentAt(new Date()) // 알림 전송 시간 설정
                    .build();

            // 알림 데이터 저장
            notificationRepository.save(notification);

            // 성공 로그 출력
            log.info("주문 취소 알림 저장 - 사용자: {}, 주문: {}", userId, orderId);
        } catch (Exception e) {
            // 오류 발생 시 로그 출력 및 예외 전파
            log.error("주문 취소 알림 실패 - 사용자: {}, 주문: {}", userId, orderId, e);
            throw new RuntimeException("알림 처리 중 오류 발생", e);
        }
    }
}