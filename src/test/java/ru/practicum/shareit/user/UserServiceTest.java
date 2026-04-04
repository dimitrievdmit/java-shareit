package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserCreateDTO;
import ru.practicum.shareit.user.dto.UserResponseDTO;
import ru.practicum.shareit.user.dto.UserUpdateDTO;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private static final Long EXISTING_USER_ID = 1L;
    private static final Long NON_EXISTING_USER_ID = 999L;

    // --- Тесты для create() ---

    @Test
    void create_ValidData_ShouldCreateUser() {
        // Given
        UserCreateDTO createDTO = new UserCreateDTO("test@example.com", "Test User");
        User expectedUser = new User(1L, "test@example.com", "Test User");
        UserResponseDTO expectedResponse = UserMapper.mapToResponseDTO(expectedUser);

        when(userRepository.existsByEmailIgnoreCase(createDTO.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        // When
        UserResponseDTO result = userService.create(createDTO);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        verify(userRepository, times(1)).existsByEmailIgnoreCase(createDTO.email());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void create_EmailAlreadyExists_ShouldThrowAlreadyExistException() {
        // Given
        UserCreateDTO createDTO = new UserCreateDTO("existing@example.com", "Existing User");

        when(userRepository.existsByEmailIgnoreCase(createDTO.email())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.create(createDTO))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessageContaining("Пользователь с email '" + createDTO.email() + "' уже существует");
    }

    // --- Тесты для get() ---

    @Test
    void get_ExistingUser_ShouldReturnUser() {
        // Given
        User existingUser = new User(EXISTING_USER_ID, "user@example.com", "User Name");
        UserResponseDTO expectedResponse = UserMapper.mapToResponseDTO(existingUser);

        when(userRepository.findById(EXISTING_USER_ID)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsById(EXISTING_USER_ID)).thenReturn(true);

        // When
        UserResponseDTO result = userService.get(EXISTING_USER_ID);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        verify(userRepository).findById(EXISTING_USER_ID);
    }

    @Test
    void get_NonExistingUser_ShouldThrowNotFoundException() {
        // Given
        when(userRepository.existsById(NON_EXISTING_USER_ID)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.get(NON_EXISTING_USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = " + NON_EXISTING_USER_ID + " не найден");
    }

    // --- Тесты для update() ---

    @Test
    void update_ExistingUserWithNewEmail_ShouldUpdateUser() {
        // Given
        Long userId = 1L;
        UserUpdateDTO updateDTO = new UserUpdateDTO("newemail@example.com", "Updated Name");
        User existingUser = new User(userId, "oldemail@example.com", "Old Name");

        // Создаём ожидаемый результат — новый объект с обновлёнными данными
        User expectedUpdatedUser = new User(
                userId,
                "newemail@example.com",
                "Updated Name"
        );
        UserResponseDTO expectedResponse = UserMapper.mapToResponseDTO(expectedUpdatedUser);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.existsByEmailIgnoreCase(updateDTO.email())).thenReturn(false);
        // Указываем, что репозиторий должен вернуть ожидаемый обновлённый объект
        when(userRepository.save(any(User.class))).thenReturn(expectedUpdatedUser);

        // When
        UserResponseDTO result = userService.update(userId, updateDTO);

        // Then
        assertThat(result).isEqualTo(expectedResponse);

        // Проверяем, что update был вызван ровно один раз с любым объектом User
        verify(userRepository, times(1)).save(any(User.class));

        // Используем ArgumentCaptor для проверки переданного в update объекта
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        // Проверяем поля захваченного объекта
        assertThat(capturedUser.getId()).isEqualTo(userId);
        assertThat(capturedUser.getEmail()).isEqualTo("newemail@example.com");
        assertThat(capturedUser.getName()).isEqualTo("Updated Name");

        // Убеждаемся, что исходный объект не изменился (важно для record)
        assertThat(existingUser.getEmail()).isEqualTo("oldemail@example.com");
        assertThat(existingUser.getName()).isEqualTo("Old Name");
    }


    @Test
    void update_ExistingUserWithSameEmail_ShouldUpdateNameOnly() {
        // Given
        Long userId = 1L;
        UserUpdateDTO updateDTO = new UserUpdateDTO(null, "Updated Name");
        User existingUser = new User(userId, "email@example.com", "Old Name");

        // Создаём ожидаемый результат — новый объект с обновлённым именем, email сохранён
        User expectedUpdatedUser = new User(
                userId,
                "email@example.com",
                "Updated Name"
        );
        UserResponseDTO expectedResponse = UserMapper.mapToResponseDTO(expectedUpdatedUser);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsById(userId)).thenReturn(true);
        // Указываем, что репозиторий должен вернуть ожидаемый обновлённый объект
        when(userRepository.save(any(User.class))).thenReturn(expectedUpdatedUser);

        // When
        UserResponseDTO result = userService.update(userId, updateDTO);

        // Then
        assertThat(result).isEqualTo(expectedResponse);

        // Проверяем, что update был вызван ровно один раз с любым объектом User
        verify(userRepository, times(1)).save(any(User.class));

        // Используем ArgumentCaptor для проверки переданного в update объекта
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        // Проверяем, что новый объект содержит только обновлённое поле (имя), email сохранён
        assertThat(capturedUser.getId()).isEqualTo(userId);
        assertThat(capturedUser.getEmail()).isEqualTo("email@example.com"); // не изменилось
        assertThat(capturedUser.getName()).isEqualTo("Updated Name"); // изменилось

        // Убеждаемся, что исходный объект не изменился (важно для record)
        assertThat(existingUser.getEmail()).isEqualTo("email@example.com");
        assertThat(existingUser.getName()).isEqualTo("Old Name");
    }


    @Test
    void update_EmailAlreadyExists_ShouldThrowAlreadyExistException() {
        // Given
        Long userId = 1L;
        UserUpdateDTO updateDTO = new UserUpdateDTO("existing@example.com", "Updated Name");
        User existingUser = new User(userId, "old@example.com", "Old Name");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.existsByEmailIgnoreCase(updateDTO.email())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.update(userId, updateDTO))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessageContaining("Пользователь с email '" + updateDTO.email() + "' уже существует");
    }

    @Test
    void update_NonExistingUser_ShouldThrowNotFoundException() {
        // Given
        Long userId = NON_EXISTING_USER_ID;
        UserUpdateDTO updateDTO = new UserUpdateDTO("new@example.com", "Updated Name");

        when(userRepository.existsById(userId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.update(userId, updateDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = " + userId + " не найден");
    }

    // --- Тесты для delete() ---

    @Test
    void delete_ExistingUser_ShouldDeleteUser() {
        // Given
        when(userRepository.existsById(EXISTING_USER_ID)).thenReturn(true);

        // When
        userService.delete(EXISTING_USER_ID);

        // Then
        verify(userRepository, times(1)).deleteById(EXISTING_USER_ID);
    }

    @Test
    void delete_NonExistingUser_ShouldThrowNotFoundException() {
        // Given
        when(userRepository.existsById(NON_EXISTING_USER_ID)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.delete(NON_EXISTING_USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = " + NON_EXISTING_USER_ID + " не найден");
    }

    // --- Тесты для checkThatUserExists() ---

    @Test
    void throwIfNot_ShouldNotThrowException() {
        // Given
        when(userRepository.existsById(EXISTING_USER_ID)).thenReturn(true);

        // When
        userService.throwIfNotExists(EXISTING_USER_ID);

        // Then — если исключение не выброшено, тест пройден
        verify(userRepository, times(1)).existsById(EXISTING_USER_ID);
    }

    @Test
    void throwIfNot_ShouldThrowNotFoundException() {
        // Given
        when(userRepository.existsById(NON_EXISTING_USER_ID)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.throwIfNotExists(NON_EXISTING_USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = " + NON_EXISTING_USER_ID + " не найден");
    }

    // --- Дополнительные тесты для update() с частичными обновлениями ---

    @Test
    void update_OnlyEmail_ShouldUpdateEmail() {
        // Given
        Long userId = 1L;
        UserUpdateDTO updateDTO = new UserUpdateDTO("newemail@example.com", null);
        User existingUser = new User(userId, "oldemail@example.com", "User Name");

        // Создаём ожидаемый результат — новый объект с обновлённым email, имя сохранено
        User expectedUpdatedUser = new User(
                userId,
                "newemail@example.com",
                "User Name"
        );
        UserResponseDTO expectedResponse = UserMapper.mapToResponseDTO(expectedUpdatedUser);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.existsByEmailIgnoreCase(updateDTO.email())).thenReturn(false);
        // Указываем, что репозиторий должен вернуть ожидаемый обновлённый объект
        when(userRepository.save(any(User.class))).thenReturn(expectedUpdatedUser);

        // When
        UserResponseDTO result = userService.update(userId, updateDTO);

        // Then
        assertThat(result).isEqualTo(expectedResponse);

        // Проверяем, что update был вызван ровно один раз с любым объектом User
        verify(userRepository, times(1)).save(any(User.class));

        // Используем ArgumentCaptor для проверки переданного в update объекта
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        // Проверяем поля захваченного объекта — только email изменился
        assertThat(capturedUser.getId()).isEqualTo(userId);
        assertThat(capturedUser.getEmail()).isEqualTo("newemail@example.com"); // изменилось
        assertThat(capturedUser.getName()).isEqualTo("User Name"); // не изменилось

        // Убеждаемся, что исходный объект не изменился (важно для record)
        assertThat(existingUser.getEmail()).isEqualTo("oldemail@example.com");
        assertThat(existingUser.getName()).isEqualTo("User Name");
    }


    @Test
    void update_OnlyName_ShouldUpdateName() {
        // Given
        Long userId = 1L;
        UserUpdateDTO updateDTO = new UserUpdateDTO(null, "New Name");
        User existingUser = new User(userId, "email@example.com", "Old Name");

        // Создаём ожидаемый результат — новый объект с обновлённым именем, email сохранён
        User expectedUpdatedUser = new User(
                userId,
                "email@example.com",
                "New Name"
        );
        UserResponseDTO expectedResponse = UserMapper.mapToResponseDTO(expectedUpdatedUser);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsById(userId)).thenReturn(true);
        // Указываем, что репозиторий должен вернуть ожидаемый обновлённый объект
        when(userRepository.save(any(User.class))).thenReturn(expectedUpdatedUser);

        // When
        UserResponseDTO result = userService.update(userId, updateDTO);

        // Then
        assertThat(result).isEqualTo(expectedResponse);

        // Проверяем, что update был вызван ровно один раз с любым объектом User
        verify(userRepository, times(1)).save(any(User.class));

        // Используем ArgumentCaptor для проверки переданного в update объекта
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        // Проверяем поля захваченного объекта — только имя изменилось
        assertThat(capturedUser.getId()).isEqualTo(userId);
        assertThat(capturedUser.getEmail()).isEqualTo("email@example.com"); // не изменилось
        assertThat(capturedUser.getName()).isEqualTo("New Name"); // изменилось

        // Убеждаемся, что исходный объект не изменился (важно для record)
        assertThat(existingUser.getEmail()).isEqualTo("email@example.com");
        assertThat(existingUser.getName()).isEqualTo("Old Name");
    }

}
