package com.doosan.productservice.repository;

import com.doosan.common.enums.ProductCategory;
import com.doosan.productservice.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

     // 특정 카테고리에 속한 상품들을 페이징하여 조회
    Page<Product> findByCategory(ProductCategory category, Pageable pageable);

     // 상품 이름에 특정 키워드가 포함된 상품들을 페이징하여 조회
    Page<Product> findByNameContaining(String keyword, Pageable pageable);

     // 특정 카테고리에 속하면서 이름에 특정 키워드가 포함된 상품들을 페이징하여 조회
    Page<Product> findByCategoryAndNameContaining(ProductCategory category, String keyword, Pageable pageable);

}
