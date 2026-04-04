package ru.practicum.shareit.user.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.dto.UserRequestDTO;
import ru.practicum.shareit.user.dto.UserResponseDTO;
import ru.practicum.shareit.user.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {

    /**
     * Преобразует доменную модель User в UserResponseDTO.
     */
    public static UserResponseDTO mapToResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getName()
        );
    }

    /**
     * Преобразует UserCreateDTO в доменную модель User.
     * ID не передаётся — будет получен из репозитория.
     */
    public static User mapToDomain(UserRequestDTO userCreateDTO) {
        return new User(
                null,
                userCreateDTO.email(),
                userCreateDTO.name()
        );
    }

    /**
     * Преобразует userResponseDTO в доменную модель User.
     */
    public static User mapResponseToDomain(UserResponseDTO userResponseDTO) {
        return new User(
                userResponseDTO.id(),
                userResponseDTO.email(),
                userResponseDTO.name()
        );
    }

    /**
     * Обновляет данные User, создавая новый экземпляр на основе userUpdate.
     * Сохраняет неизменным ID.
     * Обновляет только не‑null значения.
     *
     * @param userUpdate User с новыми данными пользователя (могут быть null)
     * @param user       существующий пользователь, который будет использован как основа для нового объекта
     * @return новый экземпляр User с обновлёнными данными
     */
    public static User updateFromDTO(User userUpdate, User user) {
        return new User(
                user.getId(),
                (userUpdate.getEmail() != null) ? userUpdate.getEmail() : user.getEmail(),
                (userUpdate.getName() != null && !userUpdate.getName().isBlank())
                        ? userUpdate.getName()
                        : user.getName()
        );
    }

}
