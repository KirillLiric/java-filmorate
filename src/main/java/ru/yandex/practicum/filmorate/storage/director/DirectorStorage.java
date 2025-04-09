package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

public interface DirectorStorage {
    Director create(Director director);

    Director update(Director director);

    Collection<Director> getAllDirectors();

    Director getDirectorById(Long directorId);

    boolean delete(Long directorId);
}