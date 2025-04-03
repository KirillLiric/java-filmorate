package ru.yandex.practicum.filmorate.exceptions;

public class RatingNotFoundException extends NotFoundException {
    public RatingNotFoundException(String message) {
        super(message);
    }

    public RatingNotFoundException(Integer id) {
        this("Рейтинг MPA с id " + id + " не найден");
    }
}
