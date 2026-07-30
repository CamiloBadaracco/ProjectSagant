package org.example.projectsagant.repository;

import org.example.projectsagant.model.Channel;
import org.example.projectsagant.model.Notification;
import org.example.projectsagant.model.NotificationStatus;
import org.example.projectsagant.model.Priority;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository repository;

    @Test
    void save_deberiaPersistirConEstadoPendingPorDefecto() {
        Notification notification = new Notification(
                "cliente@example.com", Channel.EMAIL, "Bienvenida",
                "Gracias por registrarte", Priority.MEDIUM, Map.of("origen", "web")
        );

        Notification saved = repository.save(notification);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(saved.getAttempts()).isZero();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void findByStatus_deberiaFiltrarPorEstado() {
        repository.save(new Notification("a@x.com", Channel.LOG, "s1", "b1", Priority.LOW, Map.of()));
        Notification pending = repository.save(new Notification("b@x.com", Channel.LOG, "s2", "b2", Priority.HIGH, Map.of()));
        pending.setStatus(NotificationStatus.FAILED);
        repository.save(pending);

        List<Notification> pendientes = repository.findByStatus(NotificationStatus.PENDING);

        assertThat(pendientes).hasSize(1);
        assertThat(pendientes.get(0).getRecipient()).isEqualTo("a@x.com");
    }
}