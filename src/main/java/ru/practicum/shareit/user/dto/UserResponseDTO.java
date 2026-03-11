package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;


public record UserResponseDTO(

        @Positive(message = "Ид должен быть больше 0")
        Long id,

        @NotBlank(message = "Электронная почта не может быть пустой")
        @Email(message = "Электронная почта должна соответствовать формату электронного адреса")
        String email,

        @NotBlank(message = "Имя не может быть пустым")
        String name) {

}
