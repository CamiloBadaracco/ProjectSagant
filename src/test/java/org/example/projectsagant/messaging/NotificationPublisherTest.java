package org.example.projectsagant.messaging;

import org.example.projectsagant.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class NotificationPublisherTest {

    @Mock
    private AmqpTemplate amqpTemplate;

    @InjectMocks
    private NotificationPublisher publisher;

    @Test
    void publish_deberiaEnviarElMensajeAlExchangeYRoutingKeyCorrectos() {
        publisher.publish(42L);

        verify(amqpTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE),
                eq(RabbitMQConfig.ROUTING_KEY),
                any(NotificationMessage.class)
        );
    }
}