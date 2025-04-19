package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
public class FeedEvent {
    private int eventId;
    private long userId;
    private String eventType;
    private String operation;
    private long entityId;
    private long timestamp;
}
