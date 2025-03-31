package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {

    Collection<Film> getAllFilms();

    Optional<Film> getFilmById(Long id);

    boolean existsById(Long id);

    Film createFilm(Film film);

    Optional<Film> updateFilm(Film film);

    void deleteFilm(Long id);
}