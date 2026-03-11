package ru.yandex.practicum.shareit.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public record UserCreateDTO(

        @NotBlank(message = "Электронная почта не может быть пустой")
        @Email(message = "Электронная почта должна соответствовать формату электронного адреса")
        String email,

        @NotBlank(message = "Имя не может быть пустым")
        String name) {

}
