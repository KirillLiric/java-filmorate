package ru.yandex.practicum.filmorate.storage.feed;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.util.List;

import static ru.yandex.practicum.filmorate.storage.RowMappers.FEED_EVENT_ROW_MAPPER;
import static ru.yandex.practicum.filmorate.storage.RowMappers.toFeedEventMap;

@Repository
@Qualifier("FeedDbStorage")
public class FeedDbStorage implements FeedStorage {
    private final JdbcTemplate jdbcTemplate;

    public FeedDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<FeedEvent> getUserFeed(long userId) {
        String sql = "SELECT * FROM feed " +
                "WHERE user_id = ? ORDER BY time_stamp";
        return jdbcTemplate.query(sql, FEED_EVENT_ROW_MAPPER, userId);
    }

    @Async
    @EventListener
    public void onFeedEvent(FeedEvent event) {
        SimpleJdbcInsert inserter = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("feed")
                .usingGeneratedKeyColumns("event_id");

        inserter.execute(toFeedEventMap(event));
    }
}
