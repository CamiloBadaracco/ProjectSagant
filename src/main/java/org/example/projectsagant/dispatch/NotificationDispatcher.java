package org.example.projectsagant.dispatch;

import org.example.projectsagant.model.Channel;
import org.example.projectsagant.model.Notification;

public interface NotificationDispatcher {
    Channel supports();
    void dispatch(Notification notification);
}