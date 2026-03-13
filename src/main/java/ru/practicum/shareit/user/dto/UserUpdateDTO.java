package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import ru.practicum.shareit.validator.annotation.AtLeastOneField;

@AtLeastOneField
public record UserUpdateDTO(

        @Email(message = "Электронная почта должна соответствовать формату электронного адреса")
        String email,

        String name) {

}
