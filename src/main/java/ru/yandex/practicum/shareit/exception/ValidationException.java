package ru.yandex.practicum.shareit.exception;

@SuppressWarnings("unused")
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}