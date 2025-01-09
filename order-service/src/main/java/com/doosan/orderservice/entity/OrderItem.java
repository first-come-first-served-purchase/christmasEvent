package com.doosan.orderservice.entity;

import com.doosan.common.entity.CommonEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "`orderItem`")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderItem extends CommonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int orderId; // 주문 ID
    private int productId; // 상품 ID
    private int quantity; // 주문 수량
    private int price;  // 가격
}
