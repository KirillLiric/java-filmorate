package ru.yandex.practicum.filmorate.storage.feed;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@Qualifier("FeedDbStorage")
public class FeedDbStorage implements FeedStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;

    public FeedDbStorage(JdbcTemplate jdbcTemplate, @Qualifier("UserDbStorage") UserStorage userStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.userStorage = userStorage;
    }

    @Override
    public List<FeedEvent> getUserFeed(int userId) {
        User user = userStorage.getById(userId);
        if (user == null) {
            throw new UserNotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        String sql = "SELECT * FROM feed " +
                "WHERE user_id = ? ORDER BY time_stamp";
        return jdbcTemplate.query(sql, this::mapRowToFeed, userId);
    }

    @Async
    @EventListener
    public void onFeedEvent(FeedEvent event) {
        String sql = "INSERT INTO feed (user_id, entity_id, event_type, operation_type, time_stamp) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                event.getUserId(),
                event.getEntityId(),
                event.getEventType(),
                event.getOperation(),
                event.getTimestamp()
        );
    }

    private FeedEvent mapRowToFeed(ResultSet rs, int rowNum) throws SQLException {
        return FeedEvent.builder()
                .eventId(rs.getInt("event_id"))
                .userId(rs.getLong("user_id"))
                .entityId(rs.getLong("entity_id"))
                .eventType(rs.getString("event_type"))
                .operation(rs.getString("operation_type"))
                .timestamp(rs.getLong("time_stamp"))
                .build();
    }

}
