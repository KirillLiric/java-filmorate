package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import java.util.*;

@Repository
public class InMemoryGenreStorage implements GenreStorage {
    private final Map<Integer, Genre> genres = new HashMap<>();
    private int idCounter = 0;

    public InMemoryGenreStorage() {
        // Инициализация стандартными жанрами
        save(new Genre(1, "Комедия"));
        save(new Genre(2, "Драма"));
        save(new Genre(3, "Мультфильм"));
        save(new Genre(4, "Триллер"));
        save(new Genre(5, "Документальный"));
        save(new Genre(6, "Боевик"));
    }

    private void save(Genre genre) {
        genre.setId(++idCounter);
        genres.put(genre.getId(), genre);
    }

    @Override
    public Collection<Genre> getAll() {
        return genres.values();
    }

    @Override
    public Genre getById(int id) {
        if (!genres.containsKey(id)) {
            throw new GenreNotFoundException("Жанр с id " + id + " не найден");
        }
        return genres.get(id);
    }

    @Override
    public boolean delete(int id) {
        if (!genres.containsKey(id)) {
            throw new GenreNotFoundException("Жанр с id " + id + " не найден");
        }
        genres.remove(id);
        return true;
    }
}