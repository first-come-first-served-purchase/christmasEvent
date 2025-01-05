package com.doosan.productservice.controller;

import com.doosan.common.dto.ResponseDto;
import com.doosan.common.enums.ProductCategory;
import com.doosan.productservice.dto.ProductResponse;
import com.doosan.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
}
