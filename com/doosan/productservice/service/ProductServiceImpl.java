package com.doosan.productservice.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.doosan.productservice.dto.ResponseDto;
import com.doosan.productservice.entity.Product;
import com.doosan.productservice.repository.ProductRepository;
import com.doosan.productservice.util.ApiResponse;
import com.doosan.productservice.dto.CreateOrderReqDto;
import com.doosan.productservice.dto.ProductResponse;
import com.doosan.productservice.dto.ProductCategory;
import org.springframework.data.domain.Page;

public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public long getProductPrice(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("상품 조회 실패한 ID: " + id));
        return product.getPrice();
    }

    @Override
    public ResponseEntity<ResponseDto<Void>> updateStock(CreateOrderReqDto orderRequest) {
        try {
            Product product = productRepository.findById(orderRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("상품 조회 실패한 ID: " + orderRequest.getProductId()));
            int newStock = product.getStock() - orderRequest.getQuantity();
            if (newStock < 0) {
                throw new RuntimeException("재고 부족");
            }
            product.setStock(newStock);
            productRepository.save(product);

            return ResponseEntity.ok(
                    ResponseDto.<Void>builder()
                            .statusCode(HttpStatus.OK.value())
                            .resultMessage(ApiResponse.UPDATE_COMPLETE)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.<Void>builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .resultMessage(ApiResponse.UPDATE_ERROR)
                            .detailMessage(e.getMessage())
                            .build()
                    );
        }
    }

    @Override
    public ResponseEntity<ResponseDto<Page<ProductResponse>>> getProductsWithResponse(
            int page, int size, ProductCategory category, String keyword, String sort) {
        try {
            Page<ProductResponse> products = getProducts(page, size, category, keyword, sort);

            return ResponseEntity.ok(
                    ResponseDto.<Page<ProductResponse>>builder()
                            .statusCode(HttpStatus.OK.value())
                            .resultMessage(ApiResponse.SEARCH_COMPLETE)
                            .data(products)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.<Page<ProductResponse>>builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .resultMessage(ApiResponse.SEARCH_ERROR)
                            .detailMessage(e.getMessage())
                            .build()
                    );
        }
    }

    @Override
    public ResponseEntity<ResponseDto<ProductResponse>> getProduct(Long id) {
        try {
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
    public void updateStock(CreateOrderReqDto orderRequest) {
        Product product = productRepository.findById(orderRequest.getProductId())
            .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다. ID: " + orderRequest.getProductId()));

        int newStock = product.getQuantity() - orderRequest.getQuantity();
        if (newStock < 0) {
            throw new RuntimeException("재고가 부족합니다.");
        }

        product.setQuantity(newStock);
        productRepository.save(product);
    }
}