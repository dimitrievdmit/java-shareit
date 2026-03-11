package ru.yandex.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
     * Обновляет существующий объект User данными из UserUpdateDTO.
     * Сохраняет неизменным ID.
     * Учитывает, что поля могут быть null — обновляет только не‑null значения.
     */
    public static void updateFromDTO(UserUpdateDTO userUpdateDTO, User user) {
        if (userUpdateDTO.email() != null) {
            user.setEmail(userUpdateDTO.email());
        }
        if (userUpdateDTO.name() != null && !userUpdateDTO.name().isBlank()) {
            user.setName(userUpdateDTO.name());
        }
    }
}
