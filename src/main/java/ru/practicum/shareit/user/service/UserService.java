package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserCreateDTO;
import ru.practicum.shareit.user.dto.UserResponseDTO;
import ru.practicum.shareit.user.dto.UserUpdateDTO;
import ru.practicum.shareit.user.model.User;

public interface UserService {
    User create(User user);

    User get(Long id);

    User update(Long id, User user);

    void delete(Long id);

    void throwIfNotExists(Long id);

    User getReferenceById(Long userId);
}
