package com.doosan.christmas.wishlist.repository;

import com.doosan.christmas.wishlist.domain.WishList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // 스프링 데이터 JPA 레포지토리로 등록
public interface WishListRepository extends JpaRepository<WishList, Long> {

    // 특정 멤버 ID로 위시리스트 조회 (페이징 지원)
    Page<WishList> findByMemberId(Long memberId, Pageable pageable);

    // 특정 멤버 ID와 상품 ID로 위시리스트 항목 조회
    Optional<WishList> findByMemberIdAndProductId(Long memberId, Long productId);

    // 특정 멤버 ID와 상품 ID의 위시리스트 항목 존재 여부 확인
    boolean existsByMemberIdAndProductId(Long memberId, Long productId);

    // 특정 멤버 ID와 상품 ID의 위시리스트 항목 삭제
    void deleteByMemberIdAndProductId(Long memberId, Long productId);
}
