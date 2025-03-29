package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Пользователь с id %d не найден", id)
                ));
    }

    public User createUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (!userStorage.existsById(user.getId())) {
            throw new EntityNotFoundException("Пользователь с id " + user.getId() + " не найден");
        }

        return userStorage.updateUser(user)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Пользователь с id %d не найден", user.getId())
                ));
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Пользователь не может добавить сам себя в друзья");
        }
        if (!userStorage.existsById(userId)) {
            throw new EntityNotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (!userStorage.existsById(friendId)) {
            throw new EntityNotFoundException("Пользователь с id " + friendId + " не найден");
        }
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        user.addFriend(friendId);
        friend.addFriend(userId);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);
    }

    public void removeFriend(Long userId, Long friendId) {
        if (!userStorage.existsById(userId)) {
            throw new EntityNotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (!userStorage.existsById(friendId)) {
            throw new EntityNotFoundException("Пользователь с id " + friendId + " не найден");
        }
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        user.removeFriend(friendId);
        friend.removeFriend(userId);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);
    }

    public Collection<User> getFriends(Long userId) {
        if (!userStorage.existsById(userId)) {
            throw new EntityNotFoundException("Пользователь с id " + userId + " не найден");
        }
        User user = getUserById(userId);
        return user.getFriends().stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        if (!userStorage.existsById(userId)) {
            throw new EntityNotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (!userStorage.existsById(otherId)) {
            throw new EntityNotFoundException("Пользователь с id " + otherId + " не найден");
        }
        User user = getUserById(userId);
        User otherUser = getUserById(otherId);

        Set<Long> commonFriendIds = new HashSet<>(user.getFriends());
        commonFriendIds.retainAll(otherUser.getFriends());

        return commonFriendIds.stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }
}