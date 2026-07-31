package org.example.projectsagant.dispatch;

import org.example.projectsagant.model.Channel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DispatcherRegistryTest {

    @Test
    void resolve_deberiaDevolverElDispatcherRegistradoParaElCanal() {
        NotificationDispatcher logDispatcher = mock(NotificationDispatcher.class);
        when(logDispatcher.supports()).thenReturn(Channel.LOG);
        DispatcherRegistry registry = new DispatcherRegistry(List.of(logDispatcher));

        assertThat(registry.resolve(Channel.LOG)).isSameAs(logDispatcher);
    }

    @Test
    void resolve_siNoHayDispatcherParaElCanal_deberiaLanzarExcepcion() {
        DispatcherRegistry registry = new DispatcherRegistry(List.of());

        assertThrows(IllegalStateException.class, () -> registry.resolve(Channel.EMAIL));
    }
}