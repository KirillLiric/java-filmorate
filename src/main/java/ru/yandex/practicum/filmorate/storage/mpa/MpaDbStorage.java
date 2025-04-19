package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.RatingNotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collection;

import static ru.yandex.practicum.filmorate.storage.RowMappers.MPA_ROW_MAPPER;

@Repository
@Qualifier("MpaDbStorage")
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<MpaRating> getAllRatings() {
        String sql = "SELECT * FROM mpa_rating ORDER BY rating_id";
        return jdbcTemplate.query(sql, MPA_ROW_MAPPER);
    }

    @Override
    public MpaRating getMpaById(Integer id) {
        String sql = "SELECT * FROM mpa_rating WHERE rating_id = ?";
        return jdbcTemplate.query(sql, MPA_ROW_MAPPER, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RatingNotFoundException("Рейтинг MPA с id " + id + " не найден"));
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM mpa_rating WHERE rating_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        if (rowsAffected == 0) {
            throw new RatingNotFoundException("Рейтинг MPA с id " + id + " не найден");
        }
        return true;
    }
}