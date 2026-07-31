package org.example.projectsagant.dispatch;

import org.example.projectsagant.model.Channel;
import org.example.projectsagant.model.Notification;
import org.example.projectsagant.model.Priority;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HttpNotificationDispatcherTest {

    @Test
    void dispatch_deberiaHacerPostALaUrlDelDestinatario() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://otro-servicio.local/webhook"))
                .andExpect(method(POST))
                .andRespond(withSuccess());

        HttpNotificationDispatcher dispatcher = new HttpNotificationDispatcher(builder);
        Notification notification = new Notification(
                "http://otro-servicio.local/webhook", Channel.SERVICE, "asunto", "cuerpo", Priority.LOW, Map.of());

        dispatcher.dispatch(notification);

        server.verify();
    }

    @Test
    void dispatch_siElServicioResponde5xx_deberiaLanzarDispatchException() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://otro-servicio.local/webhook")).andRespond(withServerError());

        HttpNotificationDispatcher dispatcher = new HttpNotificationDispatcher(builder);
        Notification notification = new Notification(
                "http://otro-servicio.local/webhook", Channel.SERVICE, "asunto", "cuerpo", Priority.LOW, Map.of());

        assertThrows(DispatchException.class, () -> dispatcher.dispatch(notification));
    }
}