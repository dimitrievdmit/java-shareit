package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserCreateDTO;
import ru.practicum.shareit.user.dto.UserResponseDTO;
import ru.practicum.shareit.user.dto.UserUpdateDTO;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;


@SuppressWarnings({"unused"})
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponseDTO create(UserCreateDTO userCreateDTO) {
        log.info("Создание пользователя {}", userCreateDTO.name());

        // Проверка на дублирование email
        if (userRepository.existsByEmail(userCreateDTO.email())) {
            String errText = "Пользователь с email '" + userCreateDTO.email() + "' уже существует";
            log.error("Ошибка конфликта: {}", errText);
            throw new AlreadyExistException(errText);
        }

        User user = UserMapper.mapToDomain(userCreateDTO);
        User savedUser = userRepository.create(user);
        return UserMapper.mapToResponseDTO(savedUser);
    }


    @Override
    public UserResponseDTO get(Long id) {
        log.info("Получение пользователя по id {}", id);
        checkThatUserExists(id);
        User user = userRepository.get(id);
        return UserMapper.mapToResponseDTO(user);
    }

    @Override
    public UserResponseDTO update(Long id, UserUpdateDTO userUpdateDTO) {
        log.info("Обновление пользователя с ID {} данными: {}", id, userUpdateDTO);
        checkThatUserExists(id);

        User existingUser = userRepository.get(id);
        String currentEmail = existingUser.email();
        String newEmail = userUpdateDTO.email();

        // Проверка: если email обновляется и отличается от текущего
        if (newEmail != null && !newEmail.equals(currentEmail)) {
            // Проверка на дублирование email
            if (userRepository.existsByEmail(newEmail)) {
                String errText = "Пользователь с email '" + newEmail + "' уже существует";
                log.error("Ошибка конфликта при обновлении: {}", errText);
                throw new AlreadyExistException(errText);
            }
        }
        User newUser = UserMapper.updateFromDTO(userUpdateDTO, existingUser);
        User updatedUser = userRepository.update(newUser);
        return UserMapper.mapToResponseDTO(updatedUser);
    }


    @Override
    public void delete(Long id) {
        log.info("Удаление пользователя по id {}", id);
        checkThatUserExists(id);
        userRepository.delete(id);
    }

    @Override
    public void checkThatUserExists(Long id) {
        log.info("Проверить, что пользователь существует.");
        if (userRepository.checkIfNotExists(id)) {
            String errText = "Пользователь с id = " + id + " не найден";
            log.error("Ошибка: {}", errText);
            throw new NotFoundException(errText);
        }
    }
}
