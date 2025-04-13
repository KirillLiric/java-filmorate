package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Qualifier("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film create(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");

        int filmId = simpleJdbcInsert.executeAndReturnKey(filmToMap(film)).intValue();
        updateFilmGenres(filmId, film.getGenres());
        return getFilmById(filmId);
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE film_id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        updateFilmGenres(film.getId(), film.getGenres());
        updateFilmLikes(film.getId(), film.getLikes());
        return getFilmById(film.getId());
    }

    @Override
    public Collection<Film> getAllFilms() {
        String sql = "SELECT f.*, mr.name AS mpa_name FROM films f JOIN mpa_rating mr ON f.rating_id = mr.rating_id";
        return jdbcTemplate.query(sql, this::mapRowToFilm);
    }

    @Override
    public Film getFilmById(int id) {
        String sql = "SELECT f.*, mr.name AS mpa_name FROM films f JOIN mpa_rating mr ON f.rating_id = mr.rating_id WHERE f.film_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToFilm, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new FilmNotFoundException("Фильм с id " + id + " не найден"));
    }

    @Override
    public boolean deleteFilmById(int id) {
        String sql = "DELETE FROM films WHERE film_id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    @Override
    public Film addLike(int filmId, long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
        return getFilmById(filmId);
    }

    @Override
    public Film removeLike(int filmId, long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
        return getFilmById(filmId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = "SELECT f.*, mr.name AS mpa_name, COUNT(l.user_id) AS likes_count " +
                "FROM films f JOIN mpa_rating mr ON f.rating_id = mr.rating_id " +
                "LEFT JOIN likes l ON f.film_id = l.film_id " +
                "GROUP BY f.film_id ORDER BY likes_count DESC LIMIT ?";
        return jdbcTemplate.query(sql, this::mapRowToFilm, count);
    }

    @Override
    public List<Film> getRecommendedFilms(long userId) {

        String similarUsersQuery = "SELECT l2.user_id, COUNT(l2.film_id) AS common_likes " +
                "FROM likes l1 " +
                "JOIN likes l2 ON l1.film_id = l2.film_id AND l1.user_id != l2.user_id " +
                "WHERE l1.user_id = ? " +
                "GROUP BY l2.user_id " +
                "ORDER BY common_likes DESC " +
                "LIMIT 3";

        Long similarUserId = jdbcTemplate.queryForObject(similarUsersQuery,
                (rs, rowNum) -> rs.getLong("user_id"),
                userId);

        if (similarUserId == null) {
            return Collections.emptyList();
        }

        String recommendedFilmsQuery = "SELECT f.*, mr.name AS mpa_name " +
                "FROM films f " +
                "JOIN mpa_rating mr ON f.rating_id = mr.rating_id " +
                "JOIN likes l ON f.film_id = l.film_id " +
                "WHERE l.user_id = ? " +
                "AND f.film_id NOT IN (" +
                "   SELECT film_id FROM likes WHERE user_id = ?" +
                ")";

        return jdbcTemplate.query(recommendedFilmsQuery,
                new FilmRowMapper(),
                similarUserId,
                userId);
    }

    private static class FilmRowMapper implements RowMapper<Film> {
        @Override
        public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
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
        }
    }

    private Map<String, Object> filmToMap(Film film) {
        Map<String, Object> values = new HashMap<>();
        values.put("name", film.getName());
        values.put("description", film.getDescription());
        values.put("release_date", film.getReleaseDate());
        values.put("duration", film.getDuration());
        values.put("rating_id", film.getMpa().getId());
        return values;
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        int filmId = rs.getInt("film_id");

        return Film.builder()
                .id(filmId)
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(new MpaRating(
                        rs.getInt("rating_id"),
                        rs.getString("mpa_name")))
                .likes(getLikesForFilm(filmId))
                .genres(getOrderedGenresForFilm(filmId)) // Упорядоченные жанры
                .build();
    }

    private List<Genre> getOrderedGenresForFilm(int filmId) {
        String sql = "SELECT g.genre_id, g.name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.genre_id"; // Сортировка по ID жанра

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                        new Genre(rs.getInt("genre_id"), rs.getString("name")),
                filmId);
    }

    private Set<Long> getLikesForFilm(int filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, filmId));
    }

    private Set<Genre> getGenresForFilm(int filmId) {
        String sql = "SELECT g.genre_id, g.name" +
                " FROM film_genres fg JOIN genres g ON fg.genre_id = g.genre_id WHERE fg.film_id = ? ORDER BY g.genre_id";
        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) ->
                new Genre(rs.getInt("genre_id"), rs.getString("name")), filmId));
    }

    private void updateFilmGenres(int filmId, List<Genre> genres) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", filmId);

        if (genres != null && !genres.isEmpty()) {

            Set<Integer> uniqueGenreIds = genres.stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            List<Object[]> batchArgs = uniqueGenreIds.stream()
                    .map(genreId -> new Object[]{filmId, genreId})
                    .collect(Collectors.toList());

            jdbcTemplate.batchUpdate(
                    "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                    batchArgs
            );
        }
    }


    private void updateFilmLikes(int filmId, Set<Long> likes) {
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ?", filmId);

        if (likes != null && !likes.isEmpty()) {

            List<Object[]> batchArgs = likes.stream()
                    .map(userId -> new Object[]{filmId, userId})
                    .collect(Collectors.toList());

            jdbcTemplate.batchUpdate(
                    "INSERT INTO likes (film_id, user_id) VALUES (?, ?)",
                    batchArgs
            );
        }
    }
}