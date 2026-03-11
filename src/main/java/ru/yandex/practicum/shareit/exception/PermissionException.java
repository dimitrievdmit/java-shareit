package ru.yandex.practicum.shareit.exception;

@SuppressWarnings("unused")
public class PermissionException extends RuntimeException {
    public PermissionException(String message) {
        super(message);
    }
}
