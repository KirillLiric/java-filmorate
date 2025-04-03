package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.RatingNotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

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
        return jdbcTemplate.query(sql, this::mapRowToMpa);
    }

    @Override
    public MpaRating getMpaById(Integer id) {
        String sql = "SELECT * FROM mpa_rating WHERE rating_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToMpa, id)
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

    private MpaRating mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        return MpaRating.builder()
                .id(rs.getInt("rating_id"))
                .name(rs.getString("name"))
                .build();
    }
}