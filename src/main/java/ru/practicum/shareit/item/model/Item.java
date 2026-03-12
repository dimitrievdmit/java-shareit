package ru.practicum.shareit.item.model;

import jakarta.validation.constraints.*;

import static ru.practicum.shareit.validator.Validator.MAX_DESCRIPTION_LENGTH;


public record Item(
        @Positive(message = "Ид должен быть больше 0")
        Long id,

        @NotNull(message = "Ид владельца не может быть пустым")
        @Positive(message = "Ид владельца должен быть больше 0")
        Long ownerId,

        @NotBlank(message = "Название не может быть пустым")
        String name,

        @NotBlank(message = "Описание не может быть пустым")
        @Size(max = MAX_DESCRIPTION_LENGTH, message = "Описание не может быть длиннее {max} символов")
        String description,

        @NotNull(message = "Флаг доступности для бронирования не может быть пустым")
        Boolean available,

        @Positive(message = "Ид запроса должен быть больше 0")
        Long requestId) {
}

