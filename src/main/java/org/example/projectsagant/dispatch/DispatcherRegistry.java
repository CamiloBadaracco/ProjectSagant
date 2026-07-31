package org.example.projectsagant.dispatch;

import org.example.projectsagant.model.Channel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DispatcherRegistry {

    private final Map<Channel, NotificationDispatcher> dispatchers;

    public DispatcherRegistry(List<NotificationDispatcher> dispatcherList) {
        this.dispatchers = dispatcherList.stream()
                .collect(Collectors.toMap(NotificationDispatcher::supports, Function.identity()));
    }

    public NotificationDispatcher resolve(Channel channel) {
        NotificationDispatcher dispatcher = dispatchers.get(channel);
        if (dispatcher == null) {
            throw new IllegalStateException("No hay dispatcher registrado para el canal " + channel);
        }
        return dispatcher;
    }
}