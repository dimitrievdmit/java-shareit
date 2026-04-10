package ru.practicum.shareit.user.dto;

public record UserUpdateDTO(

        String name,

        String email) implements UserRequestDTO {

}
