package ru.practicum.shareit.request.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static ru.practicum.shareit.validator.Validator.MAX_DESCRIPTION_LENGTH;

public record ItemRequestCreateDto(
        @NotBlank(message = "Описание не может быть пустым")
        @Size(max = MAX_DESCRIPTION_LENGTH, message = "Описание не может быть длиннее {max} символов")
        String description
) {
}
