package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.*;

import java.util.*;

public final class RowMappers {

    public static final RowMapper<Genre> GENRE_ROW_MAPPER = (rs, rowNum) ->
            Genre.builder()
                    .id(rs.getInt("genre_id"))
                    .name(rs.getString("name"))
                    .build();

    public static final RowMapper<MpaRating> MPA_ROW_MAPPER = (rs, rowNum) ->
            MpaRating.builder()
                    .id(rs.getInt("rating_id"))
                    .name(rs.getString("name"))
                    .build();

    public static final RowMapper<Director> DIRECTOR_ROW_MAPPER = (rs, rowNum) ->
            Director.builder()
                    .id(rs.getLong("director_id"))
                    .name(rs.getString("name"))
                    .build();

    public static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) ->
            User.builder()
                    .id(rs.getLong("user_id"))
                    .email(rs.getString("email"))
                    .login(rs.getString("login"))
                    .name(rs.getString("name"))
                    .birthday(rs.getDate("birthday").toLocalDate())
                    .build();

    public static final RowMapper<Review> REVIEW_ROW_MAPPER = (rs, rowNum) ->
            Review.builder()
                    .reviewId(rs.getInt("review_id"))
                    .userId(rs.getLong("user_id"))
                    .filmId(rs.getInt("film_id"))
                    .isPositive(rs.getBoolean("is_positive"))
                    .useful(rs.getInt("useful"))
                    .content(rs.getString("content"))
                    .build();

    public static final RowMapper<FeedEvent> FEED_EVENT_ROW_MAPPER = (rs, rowNum) ->
            FeedEvent.builder()
                    .eventId(rs.getInt("event_id"))
                    .userId(rs.getLong("user_id"))
                    .entityId(rs.getLong("entity_id"))
                    .eventType(rs.getString("event_type"))
                    .operation(rs.getString("operation_type"))
                    .timestamp(rs.getLong("time_stamp"))
                    .build();

    public static final RowMapper<Film> FILM_ROW_MAPPER = (rs, rowNum) -> {
        return Film.builder()
                .id(rs.getInt("film_id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(new MpaRating(
                        rs.getInt("rating_id"),
                        rs.getString("mpa_name")))
                .build();
    };

    public static Map<String, Object> toGenreMap(Genre genre) {
        Map<String, Object> m = new HashMap<>();
        m.put("genre_id", genre.getId());
        m.put("name", genre.getName());
        return m;
    }

    public static Map<String, Object> toMpaMap(MpaRating mpa) {
        Map<String, Object> m = new HashMap<>();
        m.put("rating_id", mpa.getId());
        m.put("name", mpa.getName());
        return m;
    }

    public static Map<String, Object> toDirectorMap(Director director) {
        Map<String, Object> m = new HashMap<>();
        m.put("director_id", director.getId());
        m.put("name", director.getName());
        return m;
    }

    public static Map<String, Object> toUserMap(User user) {
        Map<String, Object> m = new HashMap<>();
        m.put("user_id", user.getId());
        m.put("email", user.getEmail());
        m.put("login", user.getLogin());
        m.put("name", user.getName());
        m.put("birthday", user.getBirthday());
        return m;
    }

    public static Map<String, Object> toReviewMap(Review review) {
        Map<String, Object> m = new HashMap<>();
        m.put("review_id", review.getReviewId());
        m.put("user_id", review.getUserId());
        m.put("film_id", review.getFilmId());
        m.put("is_positive", review.getIsPositive());
        m.put("useful", review.getUseful());
        m.put("content", review.getContent());
        return m;
    }

    public static Map<String, Object> toFeedEventMap(FeedEvent event) {
        Map<String, Object> m = new HashMap<>();
        m.put("event_id", event.getEventId());
        m.put("user_id", event.getUserId());
        m.put("entity_id", event.getEntityId());
        m.put("event_type", event.getEventType());
        m.put("operation_type", event.getOperation());
        m.put("time_stamp", event.getTimestamp());
        return m;
    }

    public static Map<String, Object> toFilmMap(Film film) {
        Map<String, Object> m = new HashMap<>();
        m.put("film_id", film.getId());
        m.put("name", film.getName());
        m.put("description", film.getDescription());
        m.put("release_date", film.getReleaseDate());
        m.put("duration", film.getDuration());
        m.put("rating_id", film.getMpa().getId());
        return m;
    }
}
