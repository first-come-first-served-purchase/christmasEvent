package com.doosan.christmas.util;

import com.doosan.christmas.product.domain.Product;
import com.doosan.christmas.product.dto.responseDto.ProductResponseDto;
import com.doosan.christmas.product.repository.ProductRepository;
import com.doosan.christmas.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
public class ProductServiceTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @BeforeEach
    public void setUp() {
        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("A test product")
                .price(BigDecimal.valueOf(100))
                .stock(10)
                .imageUrl("http://example.com/test.jpg")
                .isActive(true)
                .build();
        productRepository.save(product);
    }


    @Test
    public void testRedisCaching() {
        log.info("Redis 캐싱 테스트 시작");

        // 1. 데이터베이스에서 첫 번째 호출
        log.info("1. 데이터베이스에서 첫 번째 호출");
        ProductResponseDto product1 = productService.getProduct(1L);

        // 2. Redis 캐시에서 두 번째 호출
        log.info("2. Redis 캐시에서 두 번째 호출");
        ProductResponseDto product2 = productService.getProduct(1L);

        // 내용 비교
        assertEquals(product1.getId(), product2.getId());
        assertEquals(product1.getName(), product2.getName());
        assertEquals(product1.getDescription(), product2.getDescription());
        assertEquals(product1.getPrice(), product2.getPrice());
        assertEquals(product1.getImageUrl(), product2.getImageUrl());

        log.info("3. 캐시 초기화 후 재조회");
        productService.clearProductsCache();

        ProductResponseDto product3 = productService.getProduct(1L);

        // 초기화 후 데이터베이스에서 다시 조회된 객체는 내용이 동일해야 함
        assertEquals(product1.getId(), product3.getId());
        assertEquals(product1.getName(), product3.getName());
        assertEquals(product1.getDescription(), product3.getDescription());
        assertEquals(product1.getPrice(), product3.getPrice());
        assertEquals(product1.getImageUrl(), product3.getImageUrl());
    }

}
