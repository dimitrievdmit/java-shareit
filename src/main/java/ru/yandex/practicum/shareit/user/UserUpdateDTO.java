package ru.yandex.practicum.shareit.user;

import jakarta.validation.constraints.Email;
import ru.yandex.practicum.shareit.validator.annotation.AtLeastOneField;

@AtLeastOneField
public record UserUpdateDTO(

        @Email(message = "Электронная почта должна соответствовать формату электронного адреса")
        String email,

        String name) {

}
