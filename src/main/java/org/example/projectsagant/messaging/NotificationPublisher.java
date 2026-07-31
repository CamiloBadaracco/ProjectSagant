package org.example.projectsagant.messaging;

import org.example.projectsagant.config.RabbitMQConfig;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationPublisher {

    private final AmqpTemplate amqpTemplate;

    public NotificationPublisher(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void publish(Long notificationId) {
        amqpTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                new NotificationMessage(notificationId)
        );
    }
}