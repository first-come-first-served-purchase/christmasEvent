package com.doosan.christmas.order.repository;

import com.doosan.christmas.order.domain.Order;
import com.doosan.christmas.order.shared.OrderStatus;
import com.doosan.christmas.order.domain.OrderSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 주문 ID와 회원 ID로 주문을 찾음
    Optional<Order> findByIdAndMemberId(Long orderId, Long memberId);

    // 주문 상태로 주문 목록을 찾음
    List<Order> findByStatus(OrderStatus status);

    // 주문 이력을 조회 (회원 ID, 상태, 날짜 범위에 따라 검색)
    @Query("SELECT o FROM Order o " +
            "WHERE o.member.id = :memberId " +
            "AND (:status is null OR o.status = :status) " +
            "AND (:startDate is null OR o.createdAt >= :startDate) " +
            "AND (:endDate is null OR o.createdAt <= :endDate)")
    Page<Order> findOrderHistory(
            @Param("memberId") Long memberId,   // 회원 ID
            @Param("status") OrderStatus status, // 주문 상태 (옵션)
            @Param("startDate") LocalDateTime startDate, // 시작 날짜 (옵션)
            @Param("endDate") LocalDateTime endDate, // 종료 날짜 (옵션)
            Pageable pageable);  // 페이지 정보

    // 주문 이력을 조회하는 기본 메서드 (OrderSearchCondition 사용)
    default Page<Order> findOrderHistory(
            Long memberId,
            OrderSearchCondition condition,  // 주문 검색 조건
            Pageable pageable) {
        return findOrderHistory(
                memberId,
                condition.getStatus(),  // 상태 필터링
                condition.getStartDate(),  // 시작일 필터링
                condition.getEndDate(),  // 종료일 필터링
                pageable  // 페이지 정보
        );
    }
}
