package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.*;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage,
                       @Qualifier("MpaDbStorage") MpaStorage mpaStorage,
                       @Qualifier("GenreDbStorage") GenreStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    public Film createFilm(Film film) {
        if (film.getMpa() != null) {
            try {
                mpaStorage.getMpaById(film.getMpa().getId());
            } catch (NotFoundException e) {
                throw new NotFoundException("MPA с id " + film.getMpa().getId() + " не найден");
            }
        }

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                try {
                    genreStorage.getById(genre.getId());
                } catch (NotFoundException e) {
                    throw new NotFoundException("Жанр с id " + genre.getId() + " не найден");
                }
            }
        }

        return filmStorage.create(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.update(film);
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(int id) {
        return filmStorage.getFilmById(id);
    }

    public Film addLike(int filmId, long userId) {
        return filmStorage.addLike(filmId, userId);
    }

    public Film removeLike(int filmId, long userId) {
        return filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }
}