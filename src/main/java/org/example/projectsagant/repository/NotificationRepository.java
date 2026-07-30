package org.example.projectsagant.repository;

import org.example.projectsagant.model.Notification;
import org.example.projectsagant.model.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByStatus(NotificationStatus status);
}