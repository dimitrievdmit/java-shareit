package ru.practicum.shareit.item.dto.item;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import ru.practicum.shareit.validator.annotation.AtLeastOneField;

import static ru.practicum.shareit.validator.Validator.MAX_DESCRIPTION_LENGTH;

@AtLeastOneField
public record ItemUpdateDTO(

        String name,

        @Size(max = MAX_DESCRIPTION_LENGTH, message = "Описание не может быть длиннее {max} символов")
        String description,

        Boolean available,

        @Positive(message = "Ид запроса должен быть больше 0")
        Long requestId
) implements ItemBaseRequestDTO {
}
