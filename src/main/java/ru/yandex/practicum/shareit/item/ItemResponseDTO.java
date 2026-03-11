package ru.yandex.practicum.shareit.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import static ru.yandex.practicum.shareit.validator.Validator.MAX_DESCRIPTION_LENGTH;


public record ItemResponseDTO(
        @Positive(message = "Ид должен быть больше 0")
        Long id,

        @NotBlank(message = "Название не может быть пустым")
        String name,

        @NotBlank(message = "Описание не может быть пустым")
        @Size(max = MAX_DESCRIPTION_LENGTH, message = "Описание не может быть длиннее {max} символов")
        String description,

        @NotNull(message = "Флаг доступности для бронирования не может быть пустым")
        Boolean available,

        @Positive(message = "Ид запроса должен быть больше 0")
        Long requestId
) {
}
