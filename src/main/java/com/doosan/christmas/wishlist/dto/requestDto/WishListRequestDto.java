package com.doosan.christmas.wishlist.dto.requestDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WishListRequestDto {

    @JsonProperty("product_id")
    @NotNull(message = "상품 ID는 필수입니다.")
    private Long productId; // 상품 ID

    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    private Long quantity; // 수량
} 