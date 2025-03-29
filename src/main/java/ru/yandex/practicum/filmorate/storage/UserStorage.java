package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;
import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    Collection<User> getAllUsers();

    Optional<User> getUserById(Long id);

    User createUser(User user);

    Optional<User> updateUser(User user);

    void deleteUser(Long id);
}