package com.doosan.productservice.service;

import com.doosan.common.dto.ResponseDto;
import com.doosan.common.dto.order.CreateOrderReqDto;
import com.doosan.common.enums.ProductCategory;
import com.doosan.productservice.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

public interface ProductService {
    Page<ProductResponse> getProducts(int page, int size, ProductCategory category, 
                                    String keyword, String sort);
                                    
    ResponseEntity<ResponseDto<Page<ProductResponse>>> getProductsWithResponse(
        int page, int size, ProductCategory category, String keyword, String sort);

    ResponseEntity<ResponseDto<ProductResponse>> getProduct(Long id);

    ResponseEntity<ResponseDto<Long>> getProductPrice(Long id);

    void updateStock(CreateOrderReqDto quantity);
}