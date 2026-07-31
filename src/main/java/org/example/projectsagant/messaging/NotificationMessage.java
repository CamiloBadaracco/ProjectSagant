package org.example.projectsagant.messaging;

public record NotificationMessage(Long notificationId, String correlationId) {
}