package com.doosan.productservice.controller;

import com.doosan.common.dto.ResponseDto;
import com.doosan.common.enums.ProductCategory;
import com.doosan.productservice.dto.ProductResponse;
import com.doosan.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 모든 상품 조회 API
     *
     * @param page    현재 페이지 (기본값: 0)
     * @param size    페이지 크기 (기본값: 10)
     * @param category 상품 카테고리 (선택 사항)
     * @param keyword  검색 키워드 (선택 사항)
     * @param sort     정렬 기준 (기본값: id)
     * @return 상품 목록과 응답 상태를 포함한 ResponseEntity
     */
    @GetMapping
    public ResponseEntity<ResponseDto<Page<ProductResponse>>> getProducts(
            @RequestParam(defaultValue = "0") int page, // 페이지 번호
            @RequestParam(defaultValue = "10") int size, // 페이지 크기
            @RequestParam(required = false) ProductCategory category, // 상품 카테고리 (옵션)
            @RequestParam(required = false) String keyword, // 검색 키워드 (옵션)
            @RequestParam(defaultValue = "id") String sort) { // 정렬 기준
        return productService.getProductsWithResponse(page, size, category, keyword, sort);
    }

    /**
     * 특정 상품 조회 API
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<ProductResponse>> getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    // circuitBreaker 테스트 용 에러 발생 api
    @GetMapping("/test/error")
    public ResponseEntity<ResponseDto<ProductResponse>> testError() {
        throw new RuntimeException("강제로 발생시킨 에러");
    }

    // circuitBreaker 테스트 용 지연 발생 api
    @GetMapping("/test/delay")
    public ResponseEntity<ResponseDto<ProductResponse>> testDelay() throws InterruptedException {
        Thread.sleep(3000); // 3초 지연
        return ResponseEntity.ok(
                ResponseDto.<ProductResponse>builder()
                        .statusCode(HttpStatus.OK.value())
                        .resultMessage("지연 응답")
                        .build()
        );
    }

    @GetMapping("/test/random-error")
    public ResponseEntity<ResponseDto<ProductResponse>> testRandomError() {
        double random = Math.random();
        if (random < 0.7) { // 70% 확률로 에러 발생
            throw new RuntimeException("랜덤 에러 발생");
        }
        return ResponseEntity.ok(
            ResponseDto.<ProductResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .resultMessage("성공")
                .build()
        );
    }

    @GetMapping("/test/timeout")
    public ResponseEntity<ResponseDto<ProductResponse>> testTimeout() {
        double random = Math.random();
        try {
            Thread.sleep((long) (random * 5000)); // 0~5초 랜덤 지연
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return ResponseEntity.ok(
            ResponseDto.<ProductResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .resultMessage("타임아웃 테스트 응답")
                .build()
        );
    }
}
