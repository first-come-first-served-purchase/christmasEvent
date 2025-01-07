package com.doosan.productservice.service;

import com.doosan.common.dto.ResponseDto;
import com.doosan.common.dto.order.CreateOrderReqDto;
import com.doosan.common.enums.ProductCategory;
import com.doosan.common.enums.ApiResponse;
import com.doosan.productservice.domain.Product;
import com.doosan.productservice.dto.ProductResponse;
import com.doosan.productservice.mapper.ProductMapper;
import com.doosan.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository; // 상품 레포지토리
    private final ProductMapper productMapper; // 상품 매퍼

    // 상품 가격 조회
    public ResponseEntity<ResponseDto<Long>> getProductPrice(Long id) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("상품 조회 실패한 ID: " + id));
            Long price = product.getPrice(); // 상품 가격 가져오기

            return ResponseEntity.ok(
                    ResponseDto.<Long>builder()
                            .statusCode(HttpStatus.OK.value())
                            .resultMessage(ApiResponse.COMPLETE) // 성공 메시지
                            .data(price)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.<Long>builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .resultMessage(ApiResponse.ERROR) // 오류 메시지
                            .detailMessage(e.getMessage())
                            .build()
                    );
        }
    }

    // 상품 재고 업데이트 ( 주문 시 )
    @Transactional
    public void updateStock(CreateOrderReqDto orderRequest) {
        Product product = productRepository.findById(orderRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다. ID: " + orderRequest.getProductId()));

        // 재고 계산 (음수면 차감, 양수면 증가)
        Long currentStock = product.getQuantity();
        Long changeAmount = orderRequest.getQuantity();
        Long newStock = currentStock + changeAmount;

        if (newStock < 0) {
            throw new RuntimeException("재고가 부족합니다. 현재 재고: " + currentStock);
        }

        product.setQuantity(newStock);
        productRepository.save(product);
        log.info("상품 재고 업데이트 완료. 상품 ID: {}, 현재 재고: {}, 변경 수량: {}, 최종 재고: {}", 
            orderRequest.getProductId(), currentStock, changeAmount, newStock);
    }

    // 상품 목록 조회 ( 페이징, 카테고리, 검색어, 정렬 지원 )
    public Page<ProductResponse> getProducts(int page, int size, ProductCategory category,
                                             String keyword, String sort) {

        Sort sorting = createSort(sort); // 정렬 조건 생성

        PageRequest pageRequest = PageRequest.of(page, size, sorting);

        // 카테고리와 검색어에 따른 조회 조건 분기
        Page<Product> products;
        if (category != null && keyword != null) {
            products = productRepository.findByCategoryAndNameContaining(category, keyword, pageRequest);
        } else if (category != null) {
            products = productRepository.findByCategory(category, pageRequest);
        } else if (keyword != null) {
            products = productRepository.findByNameContaining(keyword, pageRequest);
        } else {
            products = productRepository.findAll(pageRequest);
        }

        return products.map(productMapper::toDto);
    }

    // 단일 상품 상세 조회
    public ResponseEntity<ResponseDto<Page<ProductResponse>>> getProductsWithResponse(
            int page, int size, ProductCategory category, String keyword, String sort) {
        try {
            Page<ProductResponse> products = getProducts(page, size, category, keyword, sort);

            return ResponseEntity.ok(
                    ResponseDto.<Page<ProductResponse>>builder()
                            .statusCode(HttpStatus.OK.value())
                            .resultMessage(ApiResponse.SEARCH_COMPLETE) // 직접 상수 사용
                            .data(products)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.<Page<ProductResponse>>builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .resultMessage(ApiResponse.SEARCH_ERROR) // 직접 상수 사용
                            .detailMessage(e.getMessage())
                            .build()
                    );
        }
    }

    public ResponseEntity<ResponseDto<ProductResponse>> getProduct(Long id) {
        try {
            if (id == 999L) {
                throw new RuntimeException("테스트용 강제 에러 발생");
            }

            // 상품 조회 및 DTO 반환
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("상품 조회 실패한 ID: " + id));

            return ResponseEntity.ok(
                    ResponseDto.<ProductResponse>builder()
                            .statusCode(HttpStatus.OK.value())
                            .resultMessage(ApiResponse.SEARCH_COMPLETE)
                            .data(productMapper.toDto(product))
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.<ProductResponse>builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .resultMessage(ApiResponse.SEARCH_ERROR)
                            .detailMessage(e.getMessage())
                            .build()
                    );
        }
    }

    // 여러 상품의 가격 일괄 조회
    public ResponseEntity<ResponseDto<Map<Long, Long>>> getBulkProductPrices(List<Long> productIds) {
        try {
            // ID를 키로, 가격을 값으로 하는 Map 생성
            Map<Long, Long> priceMap = productRepository.findAllById(productIds)
                    .stream()
                    .collect(Collectors.toMap(
                            Product::getId,
                            Product::getPrice
                    ));

            return ResponseEntity.ok(
                    ResponseDto.<Map<Long, Long>>builder()
                            .statusCode(HttpStatus.OK.value())
                            .resultMessage(ApiResponse.COMPLETE)
                            .data(priceMap)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.<Map<Long, Long>>builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .resultMessage(ApiResponse.ERROR)
                            .detailMessage(e.getMessage())
                            .build()
                    );
        }
    }

    // 정렬 조건 생성 헬퍼 메서드
    private Sort createSort(String sort) {
        switch (sort.toLowerCase()) {
            case "price":
                return Sort.by(Sort.Direction.ASC, "price");
            case "price_desc":
                return Sort.by(Sort.Direction.DESC, "price");
            case "name":
                return Sort.by(Sort.Direction.ASC, "name");
            default:
                return Sort.by(Sort.Direction.ASC, "id");
        }
    }

    // Circuit Breaker 테스트용 메서드
    public ResponseEntity<ResponseDto<ProductResponse>> testRandomError() {
        try {
            double random = Math.random();
            if (random < 0.7) { // 70% 확률로 에러 발생
                throw new RuntimeException("랜덤 에러 발생");
            }
            return ResponseEntity.ok(
                    ResponseDto.<ProductResponse>builder()
                            .statusCode(HttpStatus.OK.value())
                            .resultMessage(ApiResponse.COMPLETE)
                            .data(ProductResponse.builder()
                                    .id(1L)
                                    .name("테스트 상품")
                                    .price(10000L)
                                    .build())
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.<ProductResponse>builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .resultMessage(ApiResponse.ERROR)
                            .detailMessage(e.getMessage())
                            .build()
                    );
        }
    }

    // 서킷브레이커 - 타임아웃 0~ 5초 랜덤 지연 후 기능 확인
    public ResponseEntity<ResponseDto<ProductResponse>> testTimeout() {
        try {
            Thread.sleep((long) (Math.random() * 5000)); // 0~5초 랜덤 지연
            return ResponseEntity.ok(
                    ResponseDto.<ProductResponse>builder()
                            .statusCode(HttpStatus.OK.value())
                            .resultMessage(ApiResponse.COMPLETE)
                            .data(ProductResponse.builder()
                                    .id(1L)
                                    .name("테스트 상품")
                                    .price(10000L)
                                    .build())
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.<ProductResponse>builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .resultMessage(ApiResponse.ERROR)
                            .detailMessage(e.getMessage())
                            .build()
                    );
        }
    }

}