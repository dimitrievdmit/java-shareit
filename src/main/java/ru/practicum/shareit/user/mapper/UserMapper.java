package ru.practicum.shareit.user.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.dto.UserCreateDTO;
import ru.practicum.shareit.user.dto.UserResponseDTO;
import ru.practicum.shareit.user.dto.UserUpdateDTO;
import ru.practicum.shareit.user.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {

    /**
     * Преобразует доменную модель User в UserResponseDTO.
     */
    public static UserResponseDTO mapToResponseDTO(User user) {
        return new UserResponseDTO(
                user.id(),
                user.email(),
                user.name()
        );
    }

    /**
     * Преобразует UserCreateDTO в доменную модель User.
     * ID не передаётся — будет сгенерирован при создании.
     */
    public static User mapToDomain(UserCreateDTO userCreateDTO) {
        return new User(
                null, // ID будет сгенерирован в репозитории
                userCreateDTO.email(),
                userCreateDTO.name()
        );
    }


    /**
     * Обновляет данные User, создавая новый экземпляр record на основе UserUpdateDTO.
     * Сохраняет неизменным ID.
     * Обновляет только не‑null значения.
     *
     * @param userUpdateDTO DTO с новыми данными пользователя (могут быть null)
     * @param user          существующий пользователь, который будет использован как основа для нового объекта
     * @return новый экземпляр User с обновлёнными данными
     */
    public static User updateFromDTO(UserUpdateDTO userUpdateDTO, User user) {
        return new User(
                user.id(),
                (userUpdateDTO.email() != null) ? userUpdateDTO.email() : user.email(),
                (userUpdateDTO.name() != null && !userUpdateDTO.name().isBlank())
                        ? userUpdateDTO.name()
                        : user.name()
        );
    }

}
