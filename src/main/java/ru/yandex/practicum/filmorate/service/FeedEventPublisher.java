package ru.yandex.practicum.filmorate.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.FeedEvent;

@Service
public class FeedEventPublisher {
    private final ApplicationEventPublisher publisher;

    public FeedEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish(FeedEvent event) {
        publisher.publishEvent(event);
    }
}
