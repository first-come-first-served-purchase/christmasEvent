package com.doosan.christmas.wishlist.dto.responseDto;

import com.doosan.christmas.wishlist.domain.WishList;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class WishListResponseDto {
    private Long id; // 위시 리스트 ID
    private Long productId; // 상품 iD
    private String productName; //  상품 이름
    private String description;// 상품 설명
    private BigDecimal price; //  상품 가격
    private String imageUrl;   // 상품 이미지 URL
    private Long quantity; //  상품 수량

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt; // 생성 날짜

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedAt; // 수정 날짜


    public static WishListResponseDto from(WishList wishList) { //  DTO로 변환
        return WishListResponseDto.builder()
                .id(wishList.getId()) // 위시리스트 ID
                .productId(wishList.getProduct().getId()) // 상품 ID
                .productName(wishList.getProduct().getName()) // 상품 이름
                .description(wishList.getProduct().getDescription()) // 상품 설명
                .price(wishList.getProduct().getPrice()) //  상품 가격
                .imageUrl(wishList.getProduct().getImageUrl()) // 상품 이미지
                .quantity(wishList.getQuantity())// 수량
                .createdAt(wishList.getCreatedAt()) // 생성 날짜
                .modifiedAt(wishList.getModifiedAt()) // 수정 날짜
                .build();
    }
}
