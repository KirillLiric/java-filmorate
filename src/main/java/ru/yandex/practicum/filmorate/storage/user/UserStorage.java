package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import java.util.Collection;
import java.util.List;

public interface UserStorage {

    User create(User user);

    User update(User user);

    Collection<User> getAll();

    User getById(long id);

    boolean delete(long id);

    User addFriend(long userId, long friendId);

    User removeFriend(long userId, long friendId);

    List<User> getFriends(long userId);

    List<User> getCommonFriends(long userId, long otherId);

    boolean isFriends(long userId, long friendId);

    List<Integer> getRecommendedFilms(long userId);

    boolean userExists(long userId);

}