package com.doosan.orderservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wish_list")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private int userId; // 사용자 id
    private Long productId; // 상품 id
    private int quantity; // 수량
    
    @Builder.Default
    private boolean isDeleted = false; // 삭제여부
} 