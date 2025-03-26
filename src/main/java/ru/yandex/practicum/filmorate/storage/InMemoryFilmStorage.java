package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@Qualifier("InMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {

    private final HashMap<Long, Film> films = new HashMap<>();

    public Collection<Film> getAll() {
        log.info("Получение всех фильмов");
        return  films.values();
    }

    public Film create(Film film) {
        try {
            checkFilm(film);
            film.setId(getNextId());
            films.put(film.getId(), film);
            log.info("Создан новый фильм с id: {}", film.getId());
            return film;
        } catch (ValidationException e) {
            log.error("Невозможно создать фильм: {}", e.getMessage());
            throw e;
        }
    }

    public Film update(Film film) {
        Film existingFilm;
        if (film.getId() == null) {
            log.error("Не указан id");
            throw new ValidationException("Должен быть указан id");
        }
        if (films.containsKey(film.getId())) {
            try {
                checkFilm(film);
                existingFilm = films.get(film.getId());
                existingFilm.setDescription(film.getDescription());
                existingFilm.setDuration(film.getDuration());
                existingFilm.setName(film.getName());
                existingFilm.setReleaseDate(film.getReleaseDate());
                log.info("Обновлен фильм с id: {}", film.getId());
            } catch (ValidationException e) {
                log.error("Невозможно обновить фильм: {}", e.getMessage());
                throw e;
            }
        } else {
            log.error("Существует фильм со следующим id: {}", film.getId());
            throw new ValidationException("Фильм с id = " + film.getId() + " не найден");
        }
        return existingFilm;
    }

    private void checkFilm(Film film) {
        if (film.getName().isBlank() || film.getDescription().length() > 200 ||
                film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28)) ||
                film.getDuration() <= 0) {
            throw new ValidationException("Некорректные данные фильма");
        }
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public Film addLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        if (film == null) {
            throw new ValidationException("Фильм с id = " + filmId + " не найден");
        }
        Set<Long> filmLikes = film.getLikes();
        if (filmLikes == null) {
            filmLikes = new HashSet<>();
        }
        filmLikes.add(userId);
        film.setLikes(filmLikes);
        return film;
    }

    public Film removeLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        if (film == null) {
            throw new ValidationException("Фильм с id = " + filmId + " не найден");
        }
        Set<Long> filmLikes = film.getLikes();
        if (filmLikes != null) {
            filmLikes.remove(userId);
        }
        return film;
    }

    public HashMap<Long, Film> getFilms() {
        return films;
    }

}
