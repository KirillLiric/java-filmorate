package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private static final int DEFAULT_POPULAR_COUNT = 10;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Фильм с id %d не найден", id)
                ));
    }

    public Film createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Фильм с id %d не найден", film.getId())
                ));
    }

    public void addLike(Long filmId, Long userId) {
        checkUserExists(userId);
        Film film = getFilmById(filmId);
        film.addLike(userId);
        filmStorage.updateFilm(film);
    }

    public void removeLike(Long filmId, Long userId) {
        checkUserExists(userId);
        Film film = getFilmById(filmId);
        film.removeLike(userId);
        filmStorage.updateFilm(film);
    }

    public Collection<Film> getPopularFilms(Integer count) {
        int filmsCount = count != null ? count : DEFAULT_POPULAR_COUNT;
        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                .limit(filmsCount)
                .collect(Collectors.toList());
    }

    private void checkUserExists(Long userId) {
        if (userStorage.getUserById(userId).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format("Пользователь с id %d не найден", userId)
            );
        }
    }
}