package ru.practicum.shareit.validator;


import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

import ru.practicum.shareit.exception.PermissionException;
import ru.practicum.shareit.exception.ValidationException;


@Slf4j
public class Validator {
    public static final int MAX_DESCRIPTION_LENGTH = 512;
    public static final int MAX_COMMENT_LENGTH = 512;
    public static final String ITEM_PERMISSION_ERR_TEXT = "Пользователь с ID %d не имеет прав " +
            "на выполнение операции с вещью с ID %d";
    public static final String BOOKING_PERMISSION_ERR_TEXT = "Пользователь с ID %d не имеет прав " +
            "на выполнение операции с бронированием с ID %d";
    public static final String COMMENT_CREATE_VALIDATION_ERR_TEXT = "Чтобы оставить отзыв, у пользователя должно быть хотя бы одно завершённое бронирование данной вещи.";


    public static void throwIfStartEqualsEnd(Instant start, Instant end) {
        if (start.equals(end)) {
            String errText = ("Дата и время начала бронирования start " +
                    "не должна быть равна дате и времени конца бронирования end");
            throwValidation(errText);
        }
    }

    public static void throwUnauthorised(String errText) {
        log.error("Ошибка доступа: {}", errText);
        throw new PermissionException(errText);
    }

    public static void throwValidation(String errText) {
        log.error("Ошибка валидации: {}", errText);
        throw new ValidationException(errText);
    }
}
