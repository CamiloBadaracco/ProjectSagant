package org.example.projectsagant.messaging;

import org.example.projectsagant.config.RabbitMQConfig;
import org.example.projectsagant.dispatch.DispatcherRegistry;
import org.example.projectsagant.model.Channel;
import org.example.projectsagant.model.Notification;
import org.example.projectsagant.model.NotificationStatus;
import org.example.projectsagant.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotificationRepository repository;
    private final DispatcherRegistry dispatcherRegistry;

    public NotificationConsumer(NotificationRepository repository, DispatcherRegistry dispatcherRegistry) {
        this.repository = repository;
        this.dispatcherRegistry = dispatcherRegistry;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void onMessage(NotificationMessage message) {
        Notification notification = repository.findById(message.notificationId()).orElse(null);
        if (notification == null) {
            log.warn("Notificación {} no encontrada, se descarta el mensaje", message.notificationId());
            return;
        }

        notification.setStatus(NotificationStatus.PROCESSING);
        repository.save(notification);

        boolean dispatched = attemptDispatch(notification);
        if (!dispatched) {
            notification.incrementAttempts();
            dispatched = attemptDispatch(notification); // un reintento
        }

        if (dispatched) {
            notification.setStatus(NotificationStatus.SENT);
            repository.save(notification);
        } else {
            notification.setStatus(NotificationStatus.FAILED);
            repository.save(notification);
            log.error("Notificación {} falló tras {} intentos, va a la DLQ", notification.getId(), notification.getAttempts() + 1);
            throw new AmqpRejectAndDontRequeueException("Despacho fallido para la notificación " + notification.getId());
        }
    }

    private boolean attemptDispatch(Notification notification) {
        try {
            dispatcherRegistry.resolve(Channel.LOG).dispatch(notification);
            if (notification.getChannel() != Channel.LOG) {
                dispatcherRegistry.resolve(notification.getChannel()).dispatch(notification);
            }
            return true;
        } catch (Exception e) {
            log.warn("Intento de despacho fallido para la notificación {}: {}", notification.getId(), e.getMessage());
            return false;
        }
    }
}