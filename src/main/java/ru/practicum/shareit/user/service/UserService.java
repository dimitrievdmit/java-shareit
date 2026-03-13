package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserCreateDTO;
import ru.practicum.shareit.user.dto.UserResponseDTO;
import ru.practicum.shareit.user.dto.UserUpdateDTO;

public interface UserService {
    UserResponseDTO create(UserCreateDTO userCreateDTO);

    UserResponseDTO get(Long id);

    UserResponseDTO update(Long id, UserUpdateDTO userUpdateDTO);

    void delete(Long id);

    void checkThatUserExists(Long id);
}
