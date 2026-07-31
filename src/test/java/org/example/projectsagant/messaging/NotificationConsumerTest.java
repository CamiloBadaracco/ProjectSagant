package org.example.projectsagant.messaging;

import org.example.projectsagant.dispatch.DispatchException;
import org.example.projectsagant.dispatch.DispatcherRegistry;
import org.example.projectsagant.dispatch.NotificationDispatcher;
import org.example.projectsagant.model.*;
import org.example.projectsagant.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private NotificationRepository repository;
    @Mock
    private DispatcherRegistry dispatcherRegistry;
    @Mock
    private NotificationDispatcher logDispatcher;

    @InjectMocks
    private NotificationConsumer consumer;

    @Test
    void onMessage_siElDespachoEsExitoso_deberiaMarcarComoSent() {
        Notification n = new Notification("a@x.com", Channel.LOG, "s", "b", Priority.LOW, Map.of());
        n.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(n));
        when(dispatcherRegistry.resolve(Channel.LOG)).thenReturn(logDispatcher);

        consumer.onMessage(new NotificationMessage(1L, "test-correlation"));

        assertThat(n.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void onMessage_siFallaSiempre_deberiaReintentarUnaVezYLuegoRechazarElMensaje() {
        Notification n = new Notification("a@x.com", Channel.LOG, "s", "b", Priority.LOW, Map.of());
        n.setId(2L);
        when(repository.findById(2L)).thenReturn(Optional.of(n));
        when(dispatcherRegistry.resolve(Channel.LOG)).thenReturn(logDispatcher);
        doThrow(new DispatchException("falla", new RuntimeException())).when(logDispatcher).dispatch(n);

        assertThrows(AmqpRejectAndDontRequeueException.class, () -> consumer.onMessage(new NotificationMessage(2L, "test-correlation")));

        assertThat(n.getStatus()).isEqualTo(NotificationStatus.FAILED);
        verify(logDispatcher, times(2)).dispatch(n); // intento inicial + 1 reintento
    }
}