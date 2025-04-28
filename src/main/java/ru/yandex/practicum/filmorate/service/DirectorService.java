package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@Service
public class DirectorService {
    private final DirectorStorage directorStorage;

    @Autowired
    public DirectorService(@Qualifier("DirectorDbStorage") DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public Director create(Director director) {
        return directorStorage.create(director);
    }

    public Director update(Director updateDirector) {
        if (updateDirector.getId() == null) {
            throw new ValidationException("Id режиссера должен быть указан");
        }
        return directorStorage.update(updateDirector);
    }

    public boolean delete(Long directorId) {
        return directorStorage.delete(directorId);
    }

    public Director findDirectorById(Long directorId) {
        return directorStorage.getDirectorById(directorId);
    }

    public Collection<Director> findAllDirectors() {
        return new ArrayList<>(directorStorage.getAllDirectors());
    }
}