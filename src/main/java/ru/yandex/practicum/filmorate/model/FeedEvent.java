package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
public class FeedEvent {
    int eventId;
    long userId;
    String eventType;
    String operation;
    long entityId;
    long timestamp;
}
