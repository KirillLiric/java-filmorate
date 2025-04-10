package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.SaveDataException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;

@Repository
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Director> directorRowMapper;

    public DirectorDbStorage(JdbcTemplate jdbcTemplate, DirectorRowMapper directorRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.directorRowMapper = directorRowMapper;
    }

    @Override
    public Director create(Director director) {
        String sql = "INSERT INTO directors(name)VALUES (?)";
        long id = insert(sql, director.getName());
        director.setId(id);
        return director;
    }

    @Override
    public Director update(Director director) {
        String sql = "UPDATE directors SET name = ? WHERE director_id = ?";
        int rowsUpdated = jdbcTemplate.update(sql, director.getName(), director.getId());
        if (rowsUpdated == 0) {
            throw new SaveDataException("Не удалось обновить данные режиссера");
        }
        return director;
    }

    @Override
    public Collection<Director> getAllDirectors() {
        String sql = "SELECT * FROM directors";
        return jdbcTemplate.query(sql, directorRowMapper);
    }

    @Override
    public Director getDirectorById(Long directorId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM directors WHERE director_id = ?",
                    directorRowMapper,
                    directorId
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public boolean delete(Long directorId) {
        String sql = "DELETE FROM directors WHERE director_id = ?";
        try {
            int rowsDeleted = jdbcTemplate.update(sql, directorId);
            return rowsDeleted > 0;
        } catch (DataIntegrityViolationException exception) {
            return false;
        }
    }

    private long insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);

        // Возвращаем id нового пользователя
        if (id != null) {
            return id;
        } else {
            throw new SaveDataException("Не удалось сохранить данные");
        }
    }
}
