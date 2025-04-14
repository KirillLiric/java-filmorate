package ru.yandex.practicum.filmorate.Listener;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import org.aspectj.lang.ProceedingJoinPoint;
import ru.yandex.practicum.filmorate.annotations.EventListen;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.FeedEventPublisher;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

@Aspect
@Component
public class FeedEventAspect {
    private final FeedEventPublisher eventPublisher;
    private final ReviewStorage reviewStorage;

    public FeedEventAspect(FeedEventPublisher eventPublisher, @Qualifier("ReviewDbStorage") ReviewStorage reviewStorage) {
        this.eventPublisher = eventPublisher;
        this.reviewStorage = reviewStorage;
    }

    @Around("@annotation(el)")
    public Object handleFeedEvent(ProceedingJoinPoint joinPoint, EventListen el) throws Throwable {
        long userId;
        long entityId;
        String eventType = el.eventType();
        String operation = el.operation();
        Object[] args = joinPoint.getArgs();
        Object result;

        if (eventType.equals("REVIEW") && operation.equals("REMOVE")) {
            entityId = ((Integer) args[el.entityIdArgIndex()]).longValue();
            Review review = reviewStorage.getReviewById((int) entityId);
            userId = review.getUserId();
            result = joinPoint.proceed();
            if (!((boolean) result)) {
                return false;
            }
        } else {
            result = joinPoint.proceed();

            if (!el.argIsEvent()) {
                userId = (long) args[el.userIdArgIndex()];
                entityId = (long) args[el.entityIdArgIndex()];
            } else {
                Event event = (Event) result;
                userId = event.getUserId();
                entityId = event.getEntityId();
            }
        }

        FeedEvent event = FeedEvent.builder()
                .userId(userId)
                .entityId(entityId)
                .eventType(eventType)
                .operation(operation)
                .timestamp(System.currentTimeMillis())
                .build();

        eventPublisher.publish(event);

        return result;
    }
}
