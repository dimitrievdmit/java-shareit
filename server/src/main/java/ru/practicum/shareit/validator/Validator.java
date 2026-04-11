package ru.practicum.shareit.validator;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.exception.LogicException;
import ru.practicum.shareit.exception.PermissionException;

@Slf4j
public class Validator {
    public static final String ITEM_PERMISSION_ERR_TEXT = "Пользователь с ID %d не имеет прав " +
            "на выполнение операции с вещью с ID %d";
    public static final String BOOKING_PERMISSION_ERR_TEXT = "Пользователь с ID %d не имеет прав " +
            "на выполнение операции с бронированием с ID %d";
    public static final String COMMENT_CREATE_LOGIC_ERR_TEXT = "Чтобы оставить отзыв, у пользователя должно быть хотя бы одно завершённое бронирование данной вещи.";

    public static void throwUnauthorised(String errText) {
        log.error("Ошибка доступа: {}", errText);
        throw new PermissionException(errText);
    }

    public static void throwLogicException(String errText) {
        log.error("Ошибка в логике: {}", errText);
        throw new LogicException(errText);
    }
}
