package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.annotations.EventListen;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import static ru.yandex.practicum.filmorate.storage.RowMappers.REVIEW_ROW_MAPPER;
import static ru.yandex.practicum.filmorate.storage.RowMappers.toReviewMap;

@Repository
@Qualifier("ReviewDbStorage")
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @EventListen(eventType = "REVIEW", operation = "ADD", argIsEvent = true)
    public Review createReview(Review review) {
        boolean userExists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE user_id = ?", Integer.class, review.getUserId()) > 0;
        boolean filmExists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM films WHERE film_id = ?", Integer.class, review.getFilmId()) > 0;

        if (!userExists) {
            throw new NotFoundException("Пользователь c id = " + review.getUserId() + " не найден");
        } else if (!filmExists) {
            throw new NotFoundException("Фильм с id = " + review.getFilmId() + " не найден");
        }
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reviews")
                .usingGeneratedKeyColumns("review_id");
        int id = insert.executeAndReturnKey(reviewToMap(review)).intValue();
        return getReviewById(id);
    }

    @Override
    @EventListen(eventType = "REVIEW", operation = "UPDATE", argIsEvent = true)
    public Review updateReview(Review review) {
        String sql = "UPDATE reviews SET " +
                "is_positive = ?, " +
                "content = ? " +
                "WHERE review_id = ?";

        jdbcTemplate.update(sql,
                review.getIsPositive(),
                review.getContent(),
                review.getReviewId()
        );

        return getReviewById(review.getReviewId());
    }

    @Override
    @EventListen(eventType = "REVIEW", operation = "REMOVE", entityIdArgIndex = 0)
    public boolean deleteReviewById(int id) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";

        int rowsAffected = jdbcTemplate.update(sql, id);

        return rowsAffected > 0;
    }

    @Override
    public Review getReviewById(int id) {
        try {
            String sql = "SELECT * FROM reviews WHERE review_id = ?";
            return jdbcTemplate.queryForObject(sql, this::mapRowToReview, id);
        } catch (DataAccessException e) {
            throw new NotFoundException("Отзыв с id = " + id + " не найден");
        }
    }

    @Override
    public Collection<Review> getReviewsByFilmId(int filmId, int count) {
        if (filmId == -1) {
            return getAllReviews();
        }
        String sql = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
        return jdbcTemplate.query(sql, this::mapRowToReview, filmId, count);
    }

    @Override
    public Collection<Review> getAllReviews() {
        String sql = "SELECT * FROM reviews ORDER BY useful DESC";
        return jdbcTemplate.query(sql, this::mapRowToReview);
    }

    @Override
    public Review addLike(int reviewId, int userId) {
        String deleteDisLikeSql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_positive = false";
        if (jdbcTemplate.update(deleteDisLikeSql, reviewId, userId) > 0) {
            addEstimation(reviewId, userId, true, 2);
        } else {
            addEstimation(reviewId, userId, true, 1);
        }
        return getReviewById(reviewId);
    }

    @Override
    public Review removeLike(int reviewId, int userId) {
        deleteEstimation(reviewId, userId, true, -1);
        return getReviewById(reviewId);
    }

    @Override
    public Review addDislike(int reviewId, int userId) {
        String deleteLikeSql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_positive = true";
        if (jdbcTemplate.update(deleteLikeSql, reviewId, userId) > 0) {
            addEstimation(reviewId, userId, false, -2);
        } else {
            addEstimation(reviewId, userId, false, -1);
        }
        return getReviewById(reviewId);
    }

    @Override
    public Review removeDislike(int reviewId, int userId) {
        deleteEstimation(reviewId, userId, false, 1);
        return getReviewById(reviewId);
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        return REVIEW_ROW_MAPPER.mapRow(rs, rowNum);
    }

    private Map<String, Object> reviewToMap(Review rw) {
        return toReviewMap(rw);
    }

    private void addEstimation(int reviewId, int userId, boolean isLike, int count) {
        String sql = "INSERT INTO review_likes (review_id, user_id, is_positive) VALUES (?, ?, ?)";
        int rowsAffected = jdbcTemplate.update(sql, reviewId, userId, isLike);
        if (rowsAffected > 0) {
            String updateUsefulSql = "UPDATE reviews SET useful = useful + ? WHERE review_id = ?";
            jdbcTemplate.update(updateUsefulSql, count, reviewId);
        }
    }

    private void deleteEstimation(int reviewId, int userId, boolean isLike, int count) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_positive = ?";
        int rowsAffected = jdbcTemplate.update(sql, reviewId, userId, isLike);
        if (rowsAffected > 0) {
            String updateUsefulSql = "UPDATE reviews SET useful = useful + ? WHERE review_id = ?";
            jdbcTemplate.update(updateUsefulSql, count, reviewId);
        }
    }
}
