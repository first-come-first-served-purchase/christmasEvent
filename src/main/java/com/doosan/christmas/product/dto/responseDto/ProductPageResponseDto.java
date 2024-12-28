package com.doosan.christmas.product.dto.responseDto;

import com.doosan.christmas.product.domain.Product;
import lombok.*;
import org.springframework.data.domain.Page;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor // 기본 생성자는 default 접근 수준으로 설정
@EqualsAndHashCode
public class ProductPageResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<ProductResponseDto> products; // 상품 목록
    private int currentPage; // 현재 페이지 번호
    private int totalPages; // 전체 페이지 수
    private long totalElements; // 전체 상품 수
    private boolean hasNext; // 다음 페이지 존재 여부

    /**
     * Page<Product> 객체를 ProductPageResponseDto로 변환
     */
    public static ProductPageResponseDto from(Page<Product> productPage) {
        return ProductPageResponseDto.builder()
                .products(productPage.getContent().stream().map(ProductResponseDto::from).collect(Collectors.toList()))
                .currentPage(productPage.getNumber()) // 현재 페이지 번호
                .totalPages(productPage.getTotalPages()) // 전체 페이지 수
                .totalElements(productPage.getTotalElements()) // 전체 상품 수
                .hasNext(productPage.hasNext()) // 다음 페이지 존재 여부
                .build();
    }
}