package com.doosan.orderservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "wish_list")
public class WishList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // 위시리스트 아이디
    
    private Integer userId; // 유저 아이디
    private Long productId; // 상품 아이디
    private Integer quantity; // 수량
    private boolean isDeleted; // 삭제여부
} 