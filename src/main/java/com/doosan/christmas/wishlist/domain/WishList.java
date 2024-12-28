package com.doosan.christmas.wishlist.domain;

import com.doosan.christmas.member.domain.Member;
import com.doosan.christmas.product.domain.Product;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity // 엔티티 클래스 정의
@Getter // Getter 자동 생성
@Table(name = "wish_list")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 접근 제한
public class WishList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 자동 증가
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Member와 다대일 관계 설정
    @JoinColumn(name = "member_id", nullable = false) // 외래 키 컬럼 정의
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY) // Product와 다대일 관계 설정
    @JoinColumn(name = "product_id", nullable = false) // 외래 키 컬럼 정의
    private Product product;

    @Column(nullable = false) // 수량 필드, 필수값 설정
    private Long quantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "modified_at", nullable = false)
    @LastModifiedDate
    private LocalDateTime modifiedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifiedAt = LocalDateTime.now();
    }

    @Builder // Builder 패턴으로 객체 생성
    public WishList(Member member, Product product, Long quantity) {
        this.member = member; // 멤버 할당
        this.product = product; // 상품 할당
        this.quantity = quantity; // 수량 할당
        this.createdAt = LocalDateTime.now(); // 생성 시각 설정
        this.modifiedAt = LocalDateTime.now(); // 수정 시각 설정
    }

    public void updateQuantity(Long quantity) {
        this.quantity = quantity; // 수량 업데이트
        this.modifiedAt = LocalDateTime.now(); // 수정 시각 갱신
    }
}
