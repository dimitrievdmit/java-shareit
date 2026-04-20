package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import ru.practicum.shareit.validator.annotation.AtLeastOneField;

@AtLeastOneField
public record UserUpdateDTO(

        String name,

        @Email(message = "Электронная почта должна соответствовать формату электронного адреса")
        String email) implements UserRequestDTO {

}
