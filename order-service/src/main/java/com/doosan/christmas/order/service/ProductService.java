package com.doosan.christmas.order.service;

import com.doosan.christmas.order.domain.Product;
import com.doosan.christmas.order.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public Product findProductById(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID는 필수입니다.");
        }
        return productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다. productId=" + productId));
    }

    @Transactional
    public void restoreStock(Long productId, Long quantity) {
        log.info("상품 재고 복구 시작: productId={}, quantity={}", productId, quantity);

        Product product = findProductById(productId);
        product.increaseStock(quantity.intValue());
        productRepository.save(product);

        log.info("상품 재고 복구 완료: productId={}, quantity={}", productId, quantity);
    }
} 