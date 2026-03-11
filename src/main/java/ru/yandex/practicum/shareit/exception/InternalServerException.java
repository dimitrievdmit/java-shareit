package ru.yandex.practicum.shareit.exception;

@SuppressWarnings("unused")
public class InternalServerException extends RuntimeException {
    public InternalServerException(String message) {
        super(message);
    }

    public InternalServerException(String message, Exception e) {
        super(message, e);
    }
}