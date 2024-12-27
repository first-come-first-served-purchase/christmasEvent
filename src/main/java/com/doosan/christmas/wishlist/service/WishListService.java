package com.doosan.christmas.wishlist.service;

import com.doosan.christmas.member.domain.Member;
import com.doosan.christmas.member.repository.MemberRepository;
import com.doosan.christmas.product.domain.Product;
import com.doosan.christmas.product.service.ProductService;
import com.doosan.christmas.wishlist.domain.WishList;
import com.doosan.christmas.wishlist.dto.requestDto.WishListRequestDto;
import com.doosan.christmas.wishlist.dto.responseDto.WishListResponseDto;
import com.doosan.christmas.wishlist.repository.WishListRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 위시 리스트 서비스
 *
 * 위시 리스트 에 상품을 추가, 조회, 수정, 삭제 하는 비즈니스 로직을 처리 합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WishListService {

    private final WishListRepository wishListRepository;
    private final MemberRepository memberRepository;
    private final ProductService productService;

    /**
     * 위시 리스트에 상품 추가
     *
     * @param memberId 회원 ID
     * @param request 위시 리스트에 추가할 상품 정보 요청 DTO
     * @return 추가된 위시 리스트 항목의 응답 DTO
     */
    @Transactional
    public WishListResponseDto addToWishList(Long memberId, WishListRequestDto request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        Product product = productService.findProductById(request.getProductId());

        if (wishListRepository.existsByMemberIdAndProductId(memberId, request.getProductId())) {
            throw new IllegalStateException("이미 위시리스트에 존재하는 상품입니다.");
        }

        WishList wishList = WishList.builder()
                .member(member)
                .product(product)
                .quantity(request.getQuantity())
                .build();

        wishListRepository.save(wishList);
        return WishListResponseDto.from(wishList);
    }

    /**
     * 위시 리스트 조회
     *
     * @param memberId 회원 ID
     * @param pageable 페이징 처리를 위한 객체
     * @return 회원의 위시 리스트 항목 (페이징 처리됨)
     */
    @Transactional(readOnly = true)
    public Page<WishListResponseDto> getWishList(Long memberId, Pageable pageable) {
        Page<WishList> wishListPage = wishListRepository.findByMemberId(memberId, pageable);
        return wishListPage.map(WishListResponseDto::from);
    }

    /**
     * 위시 리스트 항목 수량 수정
     *
     * @param memberId 회원 ID
     * @param productId 수정할 상품의 ID
     * @param quantity 수정할 수량
     * @return 수정된 위시 리스트 항목의 응답 DTO
     */
    @Transactional
    public WishListResponseDto updateWishListQuantity(Long memberId, Long productId, Long quantity) {
        WishList wishList = wishListRepository.findByMemberIdAndProductId(memberId, productId)
                .orElseThrow(() -> new EntityNotFoundException("위시리스트 항목을 찾을 수 없습니다."));

        wishList.updateQuantity(quantity);
        return WishListResponseDto.from(wishList);
    }

    /**
     * 위시 리스트 항목 삭제
     *
     * @param memberId 회원 ID
     * @param productId 삭제할 상품의 ID
     */
    @Transactional
    public void removeFromWishList(Long memberId, Long productId) {
        wishListRepository.deleteByMemberIdAndProductId(memberId, productId);
    }
}