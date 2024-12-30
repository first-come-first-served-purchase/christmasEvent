package com.doosan.christmas.product.service;

import com.doosan.christmas.common.exception.CustomException;
import com.doosan.christmas.common.exception.ErrorCode;
import com.doosan.christmas.product.domain.Product;
import com.doosan.christmas.product.domain.ProductCategory;
import com.doosan.christmas.product.domain.ProductSpecification;
import com.doosan.christmas.product.dto.responseDto.ProductPageResponseDto;
import com.doosan.christmas.product.dto.responseDto.ProductResponseDto;
import com.doosan.christmas.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * 상품 목록 조회 (페이징, 필터링, 검색)
     */
    @Cacheable(value = "products", key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + #category + '_' + #keyword", unless = "#result == null")
    @Transactional(readOnly = true)
    public ProductPageResponseDto getProducts(Pageable pageable, ProductCategory category, String keyword) {
        String cacheKey = String.format("products::%d_%d_%s_%s", 
            pageable.getPageNumber(), pageable.getPageSize(), category, keyword);
        log.info("상품 목록 캐시 조회 시도: {}", cacheKey);

        // 동적 쿼리 생성
        Specification<Product> spec = Specification.where(ProductSpecification.isActive())
                .and(ProductSpecification.withCategory(category))
                .and(ProductSpecification.withNameLike(keyword));

        // 조건에 맞는 상품 페이지 조회
        Page<Product> productPage = productRepository.findAll(spec, pageable);
        log.info("상품 목록 DB 조회 완료: 총 {}건 (캐시에 저장됨: {})", 
            productPage.getTotalElements(), cacheKey);

        return ProductPageResponseDto.from(productPage);
    }

    /**
     * 특정 상품 상세 조회
     */
    @Cacheable(value = "product", key = "#productId", unless = "#result == null")
    @Transactional(readOnly = true)
    public ProductResponseDto getProduct(Long productId) {
        String cacheKey = String.format("product::%d", productId);
        log.info("상품 상세 캐시 조회 시도: {}", cacheKey);
        
        try {
            Product product = productRepository.findByIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
                
            log.info("상품 상세 DB 조회 완료: productId={} (캐시에 저장됨: {})", 
                productId, cacheKey);
            return ProductResponseDto.from(product);
        } catch (Exception e) {
            log.error("상품 조회 중 오류 발생: productId={}, error={}", productId, e.getMessage());
            throw e;
        }
    }

    /**
     * 상품 재고 복구
     */
    @Transactional
    @CacheEvict(value = "product", key = "#productId")
    public void restoreStock(Long productId, Long quantity) {
        log.info("상품 재고 복구 시작: productId={}, quantity={}", productId, quantity);

        Product product = findProductById(productId);
        product.increaseStock(quantity);
        productRepository.save(product);

        log.info("상품 재고 복구 완료: productId={}, quantity={}", productId, quantity);
    }

    /**
     * 상품 조회 (내부용)
     */
    public Product findProductById(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID는 필수입니다.");
        }
        return productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다. productId=" + productId));
    }

    /**
     * 상품 캐시 초기화
     */
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public void clearProductCache() {
        log.info("모든 상품 캐시 초기화 완료 (products, product)");
    }
} 