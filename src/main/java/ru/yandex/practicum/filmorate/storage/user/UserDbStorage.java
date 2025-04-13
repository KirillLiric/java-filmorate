package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Qualifier("UserDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User getById(long id) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM users WHERE user_id = ?",
                    this::mapRowToUser,
                    id
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public User create(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");

        long userId = simpleJdbcInsert.executeAndReturnKey(userToMap(user)).longValue();
        return getById(userId);
    }

    @Override
    public User update(User user) {
        try {
            String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
            jdbcTemplate.update(sql,
            user.getEmail(),
            user.getLogin(),
            user.getName(),
            user.getBirthday(),
            user.getId());
            updateFriendships(user.getId(), user.getFriends());
            return getById(user.getId());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Collection<User> getAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, this::mapRowToUser);
    }

    @Override
    public boolean delete(long id) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    @Override
    public User addFriend(long userId, long friendId) {
        String sql = "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
        return getById(userId);
    }

    @Override
    public User removeFriend(long userId, long friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
        return getById(userId);
    }

    @Override
    public List<User> getFriends(long userId) {
        try {
            String sql = "SELECT u.* FROM users u JOIN friendships f ON u.user_id = f.friend_id WHERE f.user_id = ?";
            return jdbcTemplate.query(sql, this::mapRowToUser, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<User> getCommonFriends(long userId, long otherId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f1 ON u.user_id = f1.friend_id " +
                "JOIN friendships f2 ON u.user_id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToUser, userId, otherId);
    }

    @Override
    public List<Film> getRecommendedFilms(long userId) {

        if (!userExists(userId)) {
            throw new UserNotFoundException("User with id " + userId + " not found");
        }

        String recommendedFilmsSql = "SELECT f.*, mr.name AS mpa_name, " +
                "COUNT(DISTINCT l.user_id) AS likes_count " +
                "FROM films f " +
                "JOIN mpa_rating mr ON f.rating_id = mr.rating_id " +
                "LEFT JOIN likes l ON f.film_id = l.film_id " +
                "WHERE f.film_id NOT IN (SELECT film_id FROM likes WHERE user_id = ?) " +
                "AND EXISTS ( " +
                "   SELECT 1 FROM likes l1 " +
                "   JOIN likes l2 ON l1.film_id = l2.film_id " +
                "   WHERE l1.user_id = ? AND l2.user_id != ? " +
                ") " +
                "GROUP BY f.film_id " +
                "ORDER BY likes_count DESC";

        return jdbcTemplate.query(recommendedFilmsSql, new FilmRowMapper(), userId, userId, userId);
    }

    private static class FilmRowMapper implements RowMapper<Film> {
        @Override
        public Film mapRow(ResultSet rs, int rowNum) throws SQLException {

            Film film = Film.builder()
                    .id(rs.getInt("film_id"))
                    .name(rs.getString("name"))
                    .description(rs.getString("description"))
                    .releaseDate(rs.getDate("release_date").toLocalDate())
                    .duration(rs.getInt("duration"))
                    .build();

            if (rs.getObject("rating_id") != null) {
                film.setMpa(new MpaRating(
                        rs.getInt("rating_id"),
                        rs.getString("mpa_name")
                ));
            }

            return film;
        }
    }

    private boolean userExists(long userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE user_id = ?",
                Integer.class,
                userId
        );
        return count != null && count > 0;
    }

    private Map<String, Object> userToMap(User user) {
        Map<String, Object> values = new HashMap<>();
        values.put("email", user.getEmail());
        values.put("login", user.getLogin());
        values.put("name", user.getName());
        values.put("birthday", user.getBirthday());
        return values;
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getLong("user_id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .friends(getFriendsForUser(rs.getLong("user_id")))
                .build();
    }

    private Set<Long> getFriendsForUser(long userId) {
        String sql = "SELECT friend_id FROM friendships WHERE user_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, userId));
    }

    private void updateFriendships(long userId, Set<Long> friends) {
        jdbcTemplate.update("DELETE FROM friendships WHERE user_id = ?", userId);
        if (friends != null && !friends.isEmpty()) {

            List<Object[]> batchArgs = friends.stream()
                    .map(friendId -> new Object[]{userId, friendId})
                    .collect(Collectors.toList());

            jdbcTemplate.batchUpdate(
                    "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)",
                    batchArgs
            );
           }
        }

    @Override
    public boolean isFriends(long userId, long friendId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?",
                Integer.class,
                userId,
                friendId
        );
        return count != null && count > 0;
    }
}