package com.doosan.christmas.product.controller;

import com.doosan.christmas.product.domain.ProductCategory;
import com.doosan.christmas.product.dto.responseDto.ProductPageResponseDto;
import com.doosan.christmas.product.dto.responseDto.ProductResponseDto;
import com.doosan.christmas.product.dto.responseDto.ResponseDto;
import com.doosan.christmas.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 목록 조회
     *
     * @param page     현재 페이지 번호 (기본값: 0)
     * @param size     페이지 크기 (기본값: 20)
     * @param category 필터링할 상품 카테고리 (옵션)
     * @param keyword  검색 키워드 (옵션)
     * @param sort     정렬 기준과 방향 (기본값: id,desc)
     * @return 상품 목록을 포함한 응답 데이터
     */
    @GetMapping
    public ResponseEntity<ProductPageResponseDto> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "id,desc") String sort) {

        // 페이지 및 크기 유효성 검사
        if (page < 0 || size <= 0) {
            return ResponseEntity.badRequest().body(null);
        }

        try {
            // 정렬 파라미터 처리
            String[] sortParams = sort.split(",");
            Sort.Direction direction = Sort.Direction.fromString(sortParams[1]);
            Sort sortObj = Sort.by(direction, sortParams[0]);

            // 카테고리 문자열을 enum으로 변환
            ProductCategory categoryEnum = null;
            if (category != null && !category.isEmpty()) {
                try {
                    categoryEnum = ProductCategory.valueOf(category.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("잘못된 카테고리 값: {}", category);
                    // 잘못된 카테고리는 null로 처리하여 전체 검색
                }
            }

            // 페이지 요청 생성
            Pageable pageable = PageRequest.of(page, size, sortObj);
            ProductPageResponseDto response = productService.getProducts(pageable, categoryEnum, keyword);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("상품 목록 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * 특정 상품 조회
     *
     * @param productId 조회할 상품 ID
     * @return 상품 상세 정보 또는 오류 메시지
     */
    @GetMapping("/{productId}")
    public ResponseDto<?> getProduct(@PathVariable Long productId) {
        try {
            ProductResponseDto product = productService.getProduct(productId);
            log.info("상품 상세 조회 성공: productId={}", productId);
            return ResponseDto.success(product);

        } catch (IllegalArgumentException e) {
            log.error("상품을 찾을 수 없습니다: productId={}", productId, e);
            return ResponseDto.fail("PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.");

        } catch (Exception e) {
            log.error("상품 상세 조회 중 오류 발생: productId={}", productId, e);
            return ResponseDto.fail("PRODUCT_DETAIL_FAILED", "상품 정보를 가져오는 데 실패했습니다.");
        }
    }
} 