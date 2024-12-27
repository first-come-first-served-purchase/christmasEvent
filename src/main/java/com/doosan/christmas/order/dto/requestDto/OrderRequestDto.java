package com.doosan.christmas.order.dto.requestDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
public class OrderRequestDto {

    @NotNull(message = "Product ID는 필수입니다.")
    @JsonProperty("product_id")
    private Long productId;

    @NotNull(message = "주문 수량은 필수입니다.")
    @Min(value = 1, message = "주문 수량은 1개 이상이어야 합니다.")
    @JsonProperty("quantity")
    private Long quantity;
}