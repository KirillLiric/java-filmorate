package ru.yandex.practicum.filmorate.exceptions;

public class RatingNotFoundException extends NotFoundException {
    public RatingNotFoundException(String message) {
        super(message);
    }
}
