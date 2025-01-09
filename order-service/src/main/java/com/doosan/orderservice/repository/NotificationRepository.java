package com.doosan.orderservice.repository;

import com.doosan.orderservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
} 