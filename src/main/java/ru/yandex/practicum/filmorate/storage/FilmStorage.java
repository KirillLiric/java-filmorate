package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public interface FilmStorage {

    Collection<Film> getAll();

    Film create(Film film);

    Film update(Film film);

    Film addLike(Long filmId, Long userId);

    Film removeLike(Long filmId, Long userId);

    HashMap<Long, Film> getFilms();
}
