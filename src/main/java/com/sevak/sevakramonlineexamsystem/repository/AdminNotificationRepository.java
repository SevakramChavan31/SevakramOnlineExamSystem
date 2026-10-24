package com.sevak.sevakramonlineexamsystem.repository;

import com.sevak.sevakramonlineexamsystem.entity.AdminNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AdminNotificationRepository
        extends JpaRepository<AdminNotification, Long> {

    boolean existsBySourceKey(String sourceKey);

    // Fetch ALL notifications, newest first.
    List<AdminNotification> findAllByOrderByCreatedAtDesc();

    // Count all unread notifications.
    long countByReadFalse();

    // Mark ALL unread notifications as read.
    @Modifying
    @Query("UPDATE AdminNotification n SET n.read = true WHERE n.read = false")
    int markAllAsRead();
}