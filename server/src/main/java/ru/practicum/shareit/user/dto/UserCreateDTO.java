package ru.practicum.shareit.user.dto;

public record UserCreateDTO(
        String name,

        String email) implements UserRequestDTO {

}
