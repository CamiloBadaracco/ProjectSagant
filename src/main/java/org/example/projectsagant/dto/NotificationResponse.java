package org.example.projectsagant.dto;

import org.example.projectsagant.model.*;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        String recipient,
        Channel channel,
        String subject,
        String body,
        Priority priority,
        NotificationStatus status,
        Instant createdAt
) {
    public static NotificationResponse fromEntity(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getRecipient(), n.getChannel(), n.getSubject(),
                n.getBody(), n.getPriority(), n.getStatus(), n.getCreatedAt()
        );
    }
}