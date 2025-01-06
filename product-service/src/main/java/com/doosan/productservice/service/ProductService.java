package com.doosan.productservice.service;

import com.doosan.common.dto.ResponseDto;
import com.doosan.common.dto.order.CreateOrderReqDto;
import com.doosan.common.enums.ProductCategory;
import com.doosan.productservice.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;

public interface ProductService {
    Page<ProductResponse> getProducts(int page, int size, ProductCategory category, 
                                    String keyword, String sort);
                                    
    ResponseEntity<ResponseDto<Page<ProductResponse>>> getProductsWithResponse(
        int page, int size, ProductCategory category, String keyword, String sort);

    ResponseEntity<ResponseDto<ProductResponse>> getProduct(Long id);

    ResponseEntity<ResponseDto<Long>> getProductPrice(Long id);

    void updateStock(CreateOrderReqDto quantity);

    ResponseEntity<ResponseDto<ProductResponse>> testRandomError();
    ResponseEntity<ResponseDto<ProductResponse>> testTimeout();
      //여러 상품의 가격을 한 번에 조회
    ResponseEntity<ResponseDto<Map<Long, Long>>> getBulkProductPrices(List<Long> productIds);
}