package org.example.projectsagant.dispatch;

import org.example.projectsagant.model.Channel;
import org.example.projectsagant.model.Notification;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationDispatcher implements NotificationDispatcher {

    private final JavaMailSender mailSender;

    public EmailNotificationDispatcher(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public Channel supports() {
        return Channel.EMAIL;
    }

    @Override
    public void dispatch(Notification notification) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(notification.getRecipient());
            message.setSubject(notification.getSubject());
            message.setText(notification.getBody());
            mailSender.send(message);
        } catch (MailException e) {
            throw new DispatchException("Fallo al despachar email a " + notification.getRecipient(), e);
        }
    }
}