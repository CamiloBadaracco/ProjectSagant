package org.example.projectsagant.dispatch;

import org.example.projectsagant.model.Channel;
import org.example.projectsagant.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogNotificationDispatcher implements NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger("notifications.dispatch");

    @Override
    public Channel supports() {
        return Channel.LOG;
    }

    @Override
    public void dispatch(Notification notification) {
        log.info("notification_id={} recipient={} channel={} priority={} subject={}",
                notification.getId(), notification.getRecipient(), notification.getChannel(),
                notification.getPriority(), notification.getSubject());
    }
}