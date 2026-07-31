package org.example.projectsagant.service;

import org.example.projectsagant.dto.CreateNotificationRequest;
import org.example.projectsagant.messaging.NotificationPublisher;
import org.example.projectsagant.model.*;
import org.example.projectsagant.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;

    @Mock
    private NotificationPublisher publisher;

    @InjectMocks
    private NotificationService service;

    @Test
    void create_deberiaGuardarYPublicarEnLaCola() {
        CreateNotificationRequest request = new CreateNotificationRequest(
                "a@x.com", Channel.LOG, "asunto", "cuerpo", Priority.LOW, Map.of());
        Notification saved = new Notification("a@x.com", Channel.LOG, "asunto", "cuerpo", Priority.LOW, Map.of());
        saved.setId(1L);
        when(repository.save(any(Notification.class))).thenReturn(saved);

        Notification result = service.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        verify(publisher).publish(1L);
    }

    @Test
    void create_siFallaElPublish_igualDeberiaRetornarLaNotificacionGuardada() {
        CreateNotificationRequest request = new CreateNotificationRequest(
                "a@x.com", Channel.LOG, "asunto", "cuerpo", Priority.LOW, Map.of());
        Notification saved = new Notification("a@x.com", Channel.LOG, "asunto", "cuerpo", Priority.LOW, Map.of());
        saved.setId(2L);
        when(repository.save(any(Notification.class))).thenReturn(saved);
        doThrow(new AmqpException("sin conexión al broker")).when(publisher).publish(2L);

        Notification result = service.create(request);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getStatus()).isEqualTo(NotificationStatus.PENDING);
    }
}