package com.doosan.christmas.product.domain;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class ProductSpecification {

    /**
     * 카테고리 조건 추가
     *
     * @param category 필터링할 상품 카테고리
     * @return 카테고리 필터 조건 (카테고리가 null이면 조건 없음)
     */
    public static Specification<Product> withCategory(ProductCategory category) {
        return (root, query, cb) -> category == null ? null : cb.equal(root.get("category"), category);
    }

    /**
     * 이름 검색 조건 추가
     *
     * @param keyword 검색 키워드
     * @return 이름 필터 조건 (키워드가 null 또는 비어있으면 조건 없음)
     */
    public static Specification<Product> withNameLike(String keyword) {
        return (root, query, cb) -> StringUtils.hasText(keyword) ? cb.like(root.get("name"), "%" + keyword + "%") : null;
    }

    /**
     * 활성 상태 조건 추가
     *
     * @return 활성 상태(true)인 상품 필터 조건
     */
    public static Specification<Product> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }
}