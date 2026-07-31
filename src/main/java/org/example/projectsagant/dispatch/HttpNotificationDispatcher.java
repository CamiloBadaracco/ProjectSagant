package org.example.projectsagant.dispatch;

import org.example.projectsagant.model.Channel;
import org.example.projectsagant.model.Notification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class HttpNotificationDispatcher implements NotificationDispatcher {

    private final RestClient restClient;

    public HttpNotificationDispatcher(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @Override
    public Channel supports() {
        return Channel.SERVICE;
    }

    @Override
    public void dispatch(Notification notification) {
        try {
            restClient.post()
                    .uri(notification.getRecipient())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "subject", notification.getSubject(),
                            "body", notification.getBody(),
                            "priority", notification.getPriority()
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new DispatchException("Fallo al despachar por HTTP a " + notification.getRecipient(), e);
        }
    }
}