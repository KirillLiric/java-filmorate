package ru.yandex.practicum.filmorate.exceptions;

public class FilmNotFoundException extends NotFoundException {
    public FilmNotFoundException(String message) {
        super(message);
    }
}
