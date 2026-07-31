package org.example.projectsagant.dispatch;

import org.example.projectsagant.model.Channel;
import org.example.projectsagant.model.Notification;
import org.example.projectsagant.model.Priority;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LogNotificationDispatcherTest {

    @Test
    void dispatch_noDeberiaLanzarExcepcion() {
        LogNotificationDispatcher dispatcher = new LogNotificationDispatcher();
        Notification notification = new Notification("a@x.com", Channel.LOG, "s", "b", Priority.LOW, Map.of());

        assertDoesNotThrow(() -> dispatcher.dispatch(notification));
    }
}