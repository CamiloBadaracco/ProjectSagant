package org.example.projectsagant.dispatch;

import org.example.projectsagant.model.Channel;
import org.example.projectsagant.model.Notification;
import org.example.projectsagant.model.Priority;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailNotificationDispatcherTest {

    @Mock
    private JavaMailSender mailSender;

    @Test
    void dispatch_deberiaEnviarElEmail() {
        EmailNotificationDispatcher dispatcher = new EmailNotificationDispatcher(mailSender);
        Notification notification = new Notification("cliente@example.com", Channel.EMAIL, "asunto", "cuerpo", Priority.LOW, Map.of());

        dispatcher.dispatch(notification);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void dispatch_siFallaElEnvio_deberiaLanzarDispatchException() {
        doThrow(new MailSendException("sin conexión a SMTP")).when(mailSender).send(any(SimpleMailMessage.class));
        EmailNotificationDispatcher dispatcher = new EmailNotificationDispatcher(mailSender);
        Notification notification = new Notification("cliente@example.com", Channel.EMAIL, "asunto", "cuerpo", Priority.LOW, Map.of());

        assertThrows(DispatchException.class, () -> dispatcher.dispatch(notification));
    }
}