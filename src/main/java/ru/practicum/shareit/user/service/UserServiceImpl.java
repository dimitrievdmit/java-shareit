package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserCreateDTO;
import ru.practicum.shareit.user.dto.UserResponseDTO;
import ru.practicum.shareit.user.dto.UserUpdateDTO;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static ru.practicum.shareit.user.mapper.UserMapper.mapToDomain;
import static ru.practicum.shareit.user.mapper.UserMapper.mapToResponseDTO;

@SuppressWarnings({"unused"})
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * Создание пользователя
     */
    @Override
    @Transactional
    public UserResponseDTO create(UserCreateDTO userCreateDTO) {
        log.info("Создание пользователя {}", userCreateDTO.name());

        // Проверка на дублирование email
        if (userRepository.existsByEmailIgnoreCase(userCreateDTO.email())) {
            String errText = "Пользователь с email '" + userCreateDTO.email() + "' уже существует";
            log.error("Ошибка конфликта: {}", errText);
            throw new AlreadyExistException(errText);
        }

        return mapToResponseDTO(userRepository.save(mapToDomain(userCreateDTO)));
    }

    /**
     * Получение пользователя по ID
     */
    @Override
    public UserResponseDTO get(Long id) {
        log.info("Получение пользователя по id {}", id);
        return mapToResponseDTO(
                userRepository.findById(id).orElseThrow(
                        () -> new NotFoundException("Пользователь с id = " + id + " не найден")
                )
        );
    }

    /**
     * Обновление пользователя
     */
    @Override
    @Transactional
    public UserResponseDTO update(Long id, UserUpdateDTO userUpdate) {
        log.info("Обновление пользователя с ID {} данными: {}", id, userUpdate);
        throwIfNotExists(id);

        User existingUser = userRepository.findById(id).orElseThrow();
        String currentEmail = existingUser.getEmail();
        String newEmail = userUpdate.email();

        // Проверка: если email обновляется и отличается от текущего
        if (newEmail != null && !newEmail.equals(currentEmail)) {
            // Проверка на дублирование email
            if (userRepository.existsByEmailIgnoreCase(newEmail)) {
                String errText = "Пользователь с email '" + newEmail + "' уже существует";
                log.error("Ошибка конфликта при обновлении: {}", errText);
                throw new AlreadyExistException(errText);
            }
        }

        User newUser = UserMapper.updateFromDTO(userUpdate, existingUser);
        return mapToResponseDTO(userRepository.save(newUser));
    }

    /**
     * Удаление пользователя
     */
    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Удаление пользователя по id {}", id);
        throwIfNotExists(id);
        userRepository.deleteById(id);
    }

    /**
     * Проверка существования пользователя
     */
    @Override
    public void throwIfNotExists(Long id) {
        log.info("Проверить, что пользователь существует.");
        if (!userRepository.existsById(id)) {
            String errText = "Пользователь с id = " + id + " не найден";
            log.error("Ошибка: {}", errText);
            throw new NotFoundException(errText);
        }
    }
}