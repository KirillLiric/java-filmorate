package ru.yandex.practicum.filmorate.exceptions;

public class SaveDataException extends  RuntimeException {
    public SaveDataException(String message) {
        super(message);
    }
}
