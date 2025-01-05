package com.doosan.orderservice.repository;

import com.doosan.orderservice.entity.WishList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishListRepository extends JpaRepository<WishList, Integer> {
    List<WishList> findByUserIdAndIsDeletedFalse(Integer userId);
    boolean existsByUserIdAndProductIdAndIsDeletedFalse(Integer userId, Long productId);
    Optional<WishList> findByUserIdAndProductIdAndIsDeletedFalse(int userId, Long productId);
} 