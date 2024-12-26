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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/products")
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
    public ResponseDto<?> getProducts(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
                                      @RequestParam(required = false) ProductCategory category, @RequestParam(required = false) String keyword,
                                      @RequestParam(defaultValue = "id,desc") String sort) {

        // 페이지 및 크기 유효성 검사
        if (page < 0 || size <= 0) {
            return ResponseDto.fail("INVALID_REQUEST", "페이지와 크기는 양의 정수여야 합니다.");
        }

        try {
            // 정렬 파라미터 처리
            String[] sortParams = sort.split(",");

            Sort.Direction direction = Sort.Direction.fromString(sortParams[1]);

            Sort sortObj = Sort.by(direction, sortParams[0]);

            // 페이지 요청 생성
            Pageable pageable = PageRequest.of(page, size, sortObj);
            ProductPageResponseDto response = productService.getProducts(pageable, category, keyword);

            log.info("상품 목록 조회 성공: page={}, size={}, category={}, keyword={}", page, size, category, keyword);

            return ResponseDto.success(response);

        } catch (IllegalArgumentException e) {
            log.error("정렬 파라미터가 잘못되었습니다: sort={}", sort, e);

            return ResponseDto.fail("INVALID_SORT_PARAMETER", "정렬 파라미터 형식이 잘못되었습니다.");

        } catch (Exception e) {

            log.error("상품 목록 조회 중 오류 발생", e);
            return ResponseDto.fail("PRODUCT_LIST_FAILED", "상품 목록을 가져오는 데 실패했습니다.");
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

            return ResponseDto.success(product); // 성공 응답

        } catch (IllegalArgumentException e) {
            log.error("상품을 찾을 수 없습니다: productId={}", productId, e);

            return ResponseDto.fail("PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다."); // 상품을 찾지 못한 경우

        } catch (Exception e) {
            log.error("상품 상세 조회 중 오류 발생: productId={}", productId, e);

            return ResponseDto.fail("PRODUCT_DETAIL_FAILED", "상품 정보를 가져오는 데 실패했습니다."); // 일반적인 오류
        }
    }
}
