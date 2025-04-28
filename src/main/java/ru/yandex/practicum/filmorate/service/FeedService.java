package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class FeedService {
    private final UserStorage userStorage;
    private final FeedStorage feedStorage;

    public FeedService(@Qualifier("UserDbStorage") UserStorage userStorage,
                       @Qualifier("FeedDbStorage") FeedStorage feedStorage) {
        this.userStorage = userStorage;
        this.feedStorage = feedStorage;
    }

    public List<FeedEvent> getUserFeed(long userId) {
        userStorage.getById(userId);
        return feedStorage.getUserFeed(userId);
    }
}

