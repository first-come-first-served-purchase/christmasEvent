package com.doosan.christmas.product.repository;

import com.doosan.christmas.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    
    /**
     * 활성 상태인 상품만 조회
     * @return 활성 상태인 상품 목록
     */
    List<Product> findByIsActiveTrue();

    /**
     * 특정 ID를 가진 활성 상태 상품 조회
     * @param id 상품 ID
     * @return 활성 상태인 상품 Optional
     */
    Optional<Product> findByIdAndIsActiveTrue(Long id);
} 