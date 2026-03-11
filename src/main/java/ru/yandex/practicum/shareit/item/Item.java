package ru.yandex.practicum.shareit.item;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static ru.yandex.practicum.shareit.validator.Validator.MAX_DESCRIPTION_LENGTH;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {

    @Positive(message = "Ид должен быть больше 0")
    private Long id;

    @NotNull(message = "Ид владельца не может быть пустым")
    @Positive(message = "Ид владельца должен быть больше 0")
    private Long ownerId;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @NotBlank(message = "Описание не может быть пустым")
    @Size(max = MAX_DESCRIPTION_LENGTH, message = "Описание не может быть длиннее {max} символов")
    private String description;

    @NotNull(message = "Флаг доступности для бронирования не может быть пустым")
    private Boolean available;

    @Positive(message = "Ид запроса должен быть больше 0")
    private Long requestId;

}
