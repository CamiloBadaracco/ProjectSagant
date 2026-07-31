package org.example.projectsagant.messaging;

import org.example.projectsagant.config.CorrelationIdFilter;
import org.example.projectsagant.config.RabbitMQConfig;
import org.slf4j.MDC;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationPublisher {

    private final AmqpTemplate amqpTemplate;

    public NotificationPublisher(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void publish(Long notificationId) {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        amqpTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                new NotificationMessage(notificationId, correlationId)
        );
    }
}