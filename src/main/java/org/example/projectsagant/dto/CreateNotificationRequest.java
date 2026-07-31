package org.example.projectsagant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.projectsagant.model.Channel;
import org.example.projectsagant.model.Priority;

import java.util.Map;

public record CreateNotificationRequest(
        @NotBlank(message = "El destinatario es obligatorio") String recipient,
        @NotNull(message = "El canal es obligatorio") Channel channel,
        @NotBlank(message = "El asunto es obligatorio") String subject,
        @NotBlank(message = "El cuerpo del mensaje es obligatorio") String body,
        @NotNull(message = "La prioridad es obligatoria") Priority priority,
        Map<String, String> metadata
) {
}