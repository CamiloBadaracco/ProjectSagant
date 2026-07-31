package org.example.projectsagant.service;

import org.example.projectsagant.dto.CreateNotificationRequest;
import org.example.projectsagant.messaging.NotificationPublisher;
import org.example.projectsagant.model.Notification;
import org.example.projectsagant.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository repository;
    private final NotificationPublisher publisher;

    public NotificationService(NotificationRepository repository, NotificationPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Transactional
    public Notification create(CreateNotificationRequest request) {
        Notification notification = new Notification(
                request.recipient(), request.channel(), request.subject(),
                request.body(), request.priority(), request.metadata()
        );
        Notification saved = repository.save(notification);

        try {
            publisher.publish(saved.getId());
        } catch (AmqpException e) {
            // El despacho está degradado, pero el REST no debe fallar por eso.
            // La notificación queda PENDING; un job de reintento la va a recoger
            // (se implementa en feature/resilience-observability).
            log.error("No se pudo encolar la notificación {}: {}", saved.getId(), e.getMessage());
        }

        return saved;
    }

    public Optional<Notification> findById(Long id) {
        return repository.findById(id);
    }
}