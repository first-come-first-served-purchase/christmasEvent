package com.doosan.orderservice.test.service;

import com.doosan.common.dto.order.CreateOrderReqDto;
import com.doosan.orderservice.dto.CreateOrderResDto;
import com.doosan.orderservice.service.OrderService;
import com.doosan.orderservice.test.config.TestKafkaConfig;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Import(TestKafkaConfig.class)
@Transactional
@SpringBootTest
@ActiveProfiles("test")
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    // 동시성 테스트
    @Test
    public void testConcurrentOrders() throws InterruptedException {

        // 동시 요청할 사용자 수 설정
        int numberOfUsers = 10;  //10, 100, 10000 유저 수 변경 하여 테스트

        ExecutorService executorService = Executors.newFixedThreadPool(200); // 스레드 풀 생성
        CountDownLatch latch = new CountDownLatch(numberOfUsers); // 동기화를 위한 CountDownLatch 생성

        // 테스트용 주문 데이터 생성
        CreateOrderReqDto orderRequest = CreateOrderReqDto.builder()
                .productId(100L) // 상품 ID 설정
                .quantity(1L) // 수량 설정
                .build();

        List<Future<CreateOrderResDto>> futures = new ArrayList<>();

        // 동시 주문 요청
        for (int i = 1; i <= numberOfUsers; i++) {
            final int userId = i; // 각 사용자 ID
            Future<CreateOrderResDto> future = executorService.submit(() -> {
                try {
                    return orderService.createOrder(userId, List.of(orderRequest)); // 주문 생성 요청

                } finally {
                    latch.countDown(); // 요청 완료 후 latch 감소

                }

            });
            futures.add(future); // 결과 저장
        }

        // 모든 요청이 완료될 때까지 대기
        latch.await(5, TimeUnit.MINUTES);

        // 결과 집계
        int successCount = 0; // 성공 횟수
        int failCount = 0; // 실패 횟수

        List<String> errors = new ArrayList<>(); // 에러 메시지 목록

        for (Future<CreateOrderResDto> future : futures) {

            try {
                CreateOrderResDto result = future.get(); // 결과 가져오기

                if (result != null && result.getOrderId() > 0) {
                    successCount++; // 성공 건수 증가

                } else {
                    failCount++; // 실패 건수 증가

                }

            } catch (Exception e) {
                failCount++; // 실패 건수 증가
                errors.add(e.getMessage()); // 에러 메시지 저장
            }

        }

        // 결과 출력
        System.out.println("=== 동시성 테스트 결과 ===");
        System.out.println("총 요청 수: " + numberOfUsers);
        System.out.println("성공: " + successCount);
        System.out.println("실패: " + failCount);
        System.out.println("주요 에러 메시지:");

        errors.stream().distinct().forEach(System.out::println); // 중복 제거 후 에러 메시지 출력

        executorService.shutdown(); // 스레드 풀 종료
    }

    // API를 통해 주문 생성 테스트
    @Test
    public void testCreateOrderApi() throws InterruptedException {
        String url = "http://localhost:8000/order-service/api/v1/orders/create-order"; // API URL 설정
        String token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcklkIjoxLCJyb2xlIjoiVVNFUiIsImV4cCI6MTczNjQyMTMxNSwiaWF0IjoxNzM2NDE5NTE1fQ.EK2RGtUUMN0O3BJ_4BMOyHPsjCcjYoFgMEO4hitirsY";

        // 요청 바디 생성
        String requestBody = """
                [{
                    "productId": 100,
                    "quantity": 1
                }]""";

        // HTTP 클라이언트 생성
        HttpClient client = HttpClient.newHttpClient(); // HttpClient 인스턴스 생성

        // 요청 실행
        for (int i = 0; i < 2; i++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url)) // 요청 URL 설정
                    .header("Content-Type", "application/json")
                    .header("Authorization", token) // 인증 헤더 설정
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody)) // POST 요청 생성
                    .build();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString()); // 요청 전송 및 응답 수신
                System.out.println("요청 " + (i + 1) + " - 상태 코드: " + response.statusCode()); // 상태 코드 출력
                System.out.println("응답 본문: " + response.body()); // 응답 본문 출력

                // 요청 간 약간의 지연 추가
                Thread.sleep(500); // 0.5초 대기

            } catch (IOException e) {
                System.err.println("요청 " + (i + 1) + " 실패: " + e.getMessage()); // 요청 실패 메시지 출력
            }

        }
    }
}
