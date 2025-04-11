package ru.yandex.practicum.filmorate.exceptions;

public class InvalidRequestException  extends  RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}