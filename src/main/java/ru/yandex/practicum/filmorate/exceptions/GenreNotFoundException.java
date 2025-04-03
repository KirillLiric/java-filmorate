package ru.yandex.practicum.filmorate.exceptions;

public class GenreNotFoundException extends NotFoundException {

    public GenreNotFoundException(String message) {
        super(message);
    }

    public GenreNotFoundException(int genreId) {
        this("Жанр с id " + genreId + " не найден");
    }

    public GenreNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
