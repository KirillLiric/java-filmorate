package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.SaveDataException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;

import static ru.yandex.practicum.filmorate.storage.RowMappers.DIRECTOR_ROW_MAPPER;

@Repository
@Qualifier("DirectorDbStorage")
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
            throw new NotFoundException("Режиссёр с ID " + director.getId() + " не найден");
        }
        return director;
    }

    @Override
    public Collection<Director> getAllDirectors() {
        String sql = "SELECT * FROM directors";
        return jdbcTemplate.query(sql, DIRECTOR_ROW_MAPPER);
    }

    @Override
    public Director getDirectorById(Long directorId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM directors WHERE director_id = ?",
                    DIRECTOR_ROW_MAPPER,
                    directorId
            );
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Режиссёр с ID " + directorId + " не найден");
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

        if (id != null) {
            return id;
        } else {
            throw new SaveDataException("Не удалось сохранить данные");
        }
    }
}