package com.doosan.christmas.product.service;

import com.doosan.christmas.product.domain.Product;
import com.doosan.christmas.product.domain.ProductCategory;
import com.doosan.christmas.product.domain.ProductSpecification;
import com.doosan.christmas.product.dto.responseDto.ProductPageResponseDto;
import com.doosan.christmas.product.dto.responseDto.ProductResponseDto;
import com.doosan.christmas.product.repository.ProductRepository;
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
    private final ProductRepository productRepository; // 상품 데이터를 처리하는 리포지토리


    // 상품 목록 조회 (캐시 사용)
    @Cacheable(value = "products", key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + #category + '_' + #keyword")
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public ProductPageResponseDto getProducts(Pageable pageable, ProductCategory category, String keyword) {
        log.info("상품 목록 조회 시작: page={}, size={}, category={}, keyword={}",
                pageable.getPageNumber(), pageable.getPageSize(), category, keyword);

        // 캐시 확인 로그
        log.info("Redis 캐시에서 'products' 키로 데이터 조회 중...");

        // 동적 쿼리 생성
        log.debug("동적 쿼리 생성 시작");
        Specification<Product> spec = Specification.where(ProductSpecification.isActive())
                .and(ProductSpecification.withCategory(category))
                .and(ProductSpecification.withNameLike(keyword));
        log.debug("동적 쿼리 생성 완료");

        // 조건에 맞는 상품 페이지 조회
        log.info("상품 페이지 조회 실행");
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        log.info("상품 페이지 조회 완료: 조회된 상품 수={}, 전체 페이지 수={}",
                productPage.getContent().size(), productPage.getTotalPages());

        // DTO 변환
        log.debug("DTO 변환 시작");
        ProductPageResponseDto responseDto = ProductPageResponseDto.from(productPage);
        log.debug("DTO 변환 완료");

        log.info("Redis 캐시 설정 완료: 키='products', 데이터가 캐시에 저장되었습니다.");
        log.info("상품 목록 조회 종료");
        return responseDto;
    }

    // 특정 상품 상세 조회 (캐시 사용)
    @Cacheable(value = "product", key = "#productId")
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public ProductResponseDto getProduct(Long productId) {
        log.info("상품 상세 조회 시작: productId={}", productId);

        // 활성 상태의 특정 상품 조회
        log.debug("데이터베이스에서 상품 조회 시작: productId={}", productId);
        Product product = productRepository.findByIdAndIsActiveTrue(productId)
                .orElseThrow(() -> {
                    log.error("상품을 찾을 수 없습니다: productId={}", productId);
                    return new IllegalArgumentException("상품을 찾을 수 없습니다.");
                });
        log.debug("데이터베이스에서 상품 조회 완료: 상품명={}, 상태={}", product.getName(), product.getIsActive());

        // DTO 변환
        log.debug("DTO 변환 시작");
        ProductResponseDto responseDto = ProductResponseDto.from(product);
        log.debug("DTO 변환 완료");

        log.info("상품 상세 조회 종료");
        return responseDto;
    }


    /**
     * 상품 캐시 초기화
     * 캐시된 모든 상품 목록 제거
     */
    @CacheEvict(value = "products", allEntries = true)
    public void clearProductsCache() {
        // 캐시 초기화 로직 (필요시 호출)
        log.info("Redis 캐시 초기화 작업 시작: 'products' 캐시 삭제 중...");
        log.info("Redis 캐시 초기화 작업 완료: 'products' 캐시가 성공적으로 삭제되었습니다.");
    }
}
