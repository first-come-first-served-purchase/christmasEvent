package com.doosan.productservice.service;

import com.doosan.common.dto.ResponseDto;
import com.doosan.common.dto.order.CreateOrderReqDto;
import com.doosan.common.enums.ApiResponse;
import com.doosan.common.enums.ProductCategory;
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

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository; // 상품 레포지토리

    private final ProductMapper productMapper; // 상품 매퍼


    @Override
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
    @Override
    public void updateStock(CreateOrderReqDto orderRequest) {
        Product product = productRepository.findById(orderRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다. ID: " + orderRequest.getProductId()));

        Long newStock = product.getQuantity() - orderRequest.getQuantity();
        if (newStock < 0) {
            throw new RuntimeException("재고가 부족합니다.");
        }

        product.setQuantity(newStock);
        productRepository.save(product);
    }

    @Override
    public Page<ProductResponse> getProducts(int page, int size, ProductCategory category,
                                             String keyword, String sort) {

        Sort sorting = createSort(sort);

        PageRequest pageRequest = PageRequest.of(page, size, sorting);

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

    @Override
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

    @Override
    public ResponseEntity<ResponseDto<ProductResponse>> getProduct(Long id) {
        try {
            // 테스트용 ID 999에 대해 강제로 예외 발생
            if (id == 999L) {
                throw new RuntimeException("테스트용 강제 에러 발생");
            }
            
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

    @Override
    public ResponseEntity<ResponseDto<ProductResponse>> testRandomError() {
        try {
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
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.<ProductResponse>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .resultMessage(e.getMessage())
                    .build()
                );
        }
    }

    @Override
    public ResponseEntity<ResponseDto<ProductResponse>> testTimeout() {
        try {
            double random = Math.random();
            Thread.sleep((long) (random * 5000)); // 0~5초 랜덤 지연
            return ResponseEntity.ok(
                ResponseDto.<ProductResponse>builder()
                    .statusCode(HttpStatus.OK.value())
                    .resultMessage("타임아웃 테스트 응답")
                    .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.<ProductResponse>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .resultMessage(e.getMessage())
                    .build()
                );
        }
    }

    @Override
    public ResponseEntity<ResponseDto<Map<Long, Long>>> getBulkProductPrices(List<Long> productIds) {
        try {
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
}
