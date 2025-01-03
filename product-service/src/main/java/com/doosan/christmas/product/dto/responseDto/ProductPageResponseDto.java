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
@NoArgsConstructor
@EqualsAndHashCode
public class ProductPageResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<ProductResponseDto> products;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean hasNext;

    /**
     * Page<Product> 객체를 ProductPageResponseDto로 변환
     */
    public static ProductPageResponseDto from(Page<Product> productPage) {
        return ProductPageResponseDto.builder()
                .products(productPage.getContent().stream()
                        .map(ProductResponseDto::from)
                        .collect(Collectors.toList()))
                .currentPage(productPage.getNumber())
                .totalPages(productPage.getTotalPages())
                .totalElements(productPage.getTotalElements())
                .hasNext(productPage.hasNext())
                .build();
    }
} 