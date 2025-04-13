package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.Collection;
import java.util.List;

public interface FilmStorage {

    Film create(Film film);

    Film update(Film film);

    Collection<Film> getAllFilms();

    Film getFilmById(int id);

    boolean deleteFilmById(int id);

    Film addLike(int filmId, long userId);

    Film removeLike(int filmId, long userId);

    List<Film> getPopularFilms(int count);

    List<Film> getRecommendedFilms(long userId);
}