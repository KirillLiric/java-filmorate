package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping("/{id}")
    public Director findDirector(@PathVariable("id") Long directorId) {
        return directorService.findDirectorById(directorId);
    }

    @GetMapping
    public Collection<Director> findAll() {
        return directorService.findAllDirectors();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Director create(@Valid @RequestBody Director newDirector) {
        return directorService.create(newDirector);
    }

    @PutMapping
    public Director update(@Valid @RequestBody Director updateDirector) {
        return directorService.update(updateDirector);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        boolean deleted = directorService.delete(id);
        if (!deleted) {
            throw new NotFoundException("Режиссер с ID " + id + " не найден");
        }
    }
}
