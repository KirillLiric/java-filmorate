package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public UserService(@Qualifier("UserDbStorage") UserStorage userStorage,
                       @Qualifier("FilmDbStorage") FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public User createUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.create(user);
    }

    public User updateUser(User user) {
        if (userStorage.getById(user.getId()) == null) {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }
        return userStorage.update(user);
    }

    public void deleteUser(long id) {
        userStorage.delete(id);
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAll();
    }

    public User getUserById(long id) {
        return userStorage.getById(id);
    }

    public User addFriend(long userId, long friendId) {
        User user = userStorage.getById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        User friend = userStorage.getById(friendId);
        if (friend == null) {
            throw new NotFoundException("Пользователь с id " + friendId + " не найден");
        }

        if (userId == friendId) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        if (user.getFriends().contains(friendId)) {
            throw new ValidationException("Пользователь уже в друзьях");
        }
        return userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        if (userStorage.getById(userId) == null || userStorage.getById(friendId) == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        userStorage.removeFriend(userId, friendId);
    }

    public List<User> getFriends(long userId) {
        if (userStorage.getFriends(userId) == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        return userStorage.getCommonFriends(userId, otherId);
    }

    public List<Film> getRecommendedFilms(long userId) {
        try {
            if (!userStorage.userExists(userId)) {
                throw new UserNotFoundException("Пользователь с id " + userId + " не найден");
            }
            List<Integer> filmIds = userStorage.getRecommendedFilms(userId);
            return filmStorage.getFilmsByIds(filmIds);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public boolean isFriends(long userId, long friendId) {
        return userStorage.isFriends(userId, friendId);
    }
}