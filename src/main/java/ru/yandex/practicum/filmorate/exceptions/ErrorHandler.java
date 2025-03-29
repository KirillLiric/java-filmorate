package ru.yandex.practicum.filmorate.exceptions;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final ValidationException e) {
        return new ErrorResponse("Ошибка валидации", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public List<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<ErrorResponse> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorResponse(
                        "Поле " + error.getField() + " не прошло валидацию",
                        error.getDefaultMessage()))
                .collect(Collectors.toList());
        log.warn("Ошибка валидации: {}", errors);
        return errors;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().iterator().next().getMessage();
        log.warn("Нарушение ограничения: {}", message);
        return new ErrorResponse("Ошибка валидации", message);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEntityNotFound(EntityNotFoundException e) {
        log.warn("Сущность не найдена: {}", e.getMessage());
        return new ErrorResponse("Сущность не найдена", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception e) {
        log.error("Внутренняя ошибка сервера", e);
        return new ErrorResponse("Внутренняя ошибка сервера", e.getMessage());
    }
}