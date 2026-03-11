package ru.yandex.practicum.shareit.user;

public interface UserService {
    UserResponseDTO create(UserCreateDTO userCreateDTO);

    UserResponseDTO get(Long id);

    UserResponseDTO update(Long id, UserUpdateDTO userUpdateDTO);

    void delete(Long id);

    void checkThatUserExists(Long id);
}
