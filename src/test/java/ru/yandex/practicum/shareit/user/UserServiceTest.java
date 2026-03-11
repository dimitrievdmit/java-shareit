package ru.yandex.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.shareit.exception.AlreadyExistException;
import ru.yandex.practicum.shareit.exception.NotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private final Long EXISTING_USER_ID = 1L;
    private final Long NON_EXISTING_USER_ID = 999L;

    // --- Тесты для create() ---

    @Test
    void create_ValidData_ShouldCreateUser() {
        // Given
        UserCreateDTO createDTO = new UserCreateDTO("test@example.com", "Test User");
        User expectedUser = new User(1L, "test@example.com", "Test User");
        UserResponseDTO expectedResponse = UserMapper.mapToResponseDTO(expectedUser);

        when(userRepository.existsByEmail(createDTO.email())).thenReturn(false);
        when(userRepository.create(any(User.class))).thenReturn(expectedUser);

        // When
        UserResponseDTO result = userService.create(createDTO);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        verify(userRepository, times(1)).existsByEmail(createDTO.email());
        verify(userRepository, times(1)).create(any(User.class));
    }

    @Test
    void create_EmailAlreadyExists_ShouldThrowAlreadyExistException() {
        // Given
        UserCreateDTO createDTO = new UserCreateDTO("existing@example.com", "Existing User");

        when(userRepository.existsByEmail(createDTO.email())).thenReturn(true);

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

        when(userRepository.get(EXISTING_USER_ID)).thenReturn(existingUser);
        when(userRepository.checkIfNotExists(EXISTING_USER_ID)).thenReturn(false);

        // When
        UserResponseDTO result = userService.get(EXISTING_USER_ID);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        verify(userRepository).get(EXISTING_USER_ID);
    }

    @Test
    void get_NonExistingUser_ShouldThrowNotFoundException() {
        // Given
        when(userRepository.checkIfNotExists(NON_EXISTING_USER_ID)).thenReturn(true);

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
        User updatedUser = new User(userId, "newemail@example.com", "Updated Name");
        UserResponseDTO expectedResponse = UserMapper.mapToResponseDTO(updatedUser);

        when(userRepository.get(userId)).thenReturn(existingUser);
        when(userRepository.checkIfNotExists(userId)).thenReturn(false);
        when(userRepository.existsByEmail(updateDTO.email())).thenReturn(false);
        when(userRepository.update(existingUser)).thenReturn(updatedUser);

        // When
        UserResponseDTO result = userService.update(userId, updateDTO);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(existingUser.getEmail()).isEqualTo("newemail@example.com");
        assertThat(existingUser.getName()).isEqualTo("Updated Name");
        verify(userRepository, times(1)).update(existingUser);
    }

    @Test
    void update_ExistingUserWithSameEmail_ShouldUpdateNameOnly() {
        // Given
        Long userId = 1L;
        UserUpdateDTO updateDTO = new UserUpdateDTO(null, "Updated Name");
        User existingUser = new User(userId, "email@example.com", "Old Name");
        User updatedUser = new User(userId, "email@example.com", "Updated Name");
        UserResponseDTO expectedResponse = UserMapper.mapToResponseDTO(updatedUser);

        when(userRepository.get(userId)).thenReturn(existingUser);
        when(userRepository.checkIfNotExists(userId)).thenReturn(false);
        when(userRepository.update(existingUser)).thenReturn(updatedUser);

        // When
        UserResponseDTO result = userService.update(userId, updateDTO);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(existingUser.getEmail()).isEqualTo("email@example.com"); // не изменилось
        assertThat(existingUser.getName()).isEqualTo("Updated Name");
    }

    @Test
    void update_EmailAlreadyExists_ShouldThrowAlreadyExistException() {
        // Given
        Long userId = 1L;
        UserUpdateDTO updateDTO = new UserUpdateDTO("existing@example.com", "Updated Name");
        User existingUser = new User(userId, "old@example.com", "Old Name");

        when(userRepository.get(userId)).thenReturn(existingUser);
        when(userRepository.checkIfNotExists(userId)).thenReturn(false);
        when(userRepository.existsByEmail(updateDTO.email())).thenReturn(true);

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

        when(userRepository.checkIfNotExists(userId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.update(userId, updateDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = " + userId + " не найден");
    }

    // --- Тесты для delete() ---

    @Test
    void delete_ExistingUser_ShouldDeleteUser() {
        // Given
        when(userRepository.checkIfNotExists(EXISTING_USER_ID)).thenReturn(false);

        // When
        userService.delete(EXISTING_USER_ID);

        // Then
        verify(userRepository, times(1)).delete(EXISTING_USER_ID);
    }

    @Test
    void delete_NonExistingUser_ShouldThrowNotFoundException() {
        // Given
        when(userRepository.checkIfNotExists(NON_EXISTING_USER_ID)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.delete(NON_EXISTING_USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = " + NON_EXISTING_USER_ID + " не найден");
    }

    // --- Тесты для checkThatUserExists() ---

    @Test
    void checkThatUserExists_ExistingUser_ShouldNotThrowException() {
        // Given
        when(userRepository.checkIfNotExists(EXISTING_USER_ID)).thenReturn(false);

        // When
        userService.checkThatUserExists(EXISTING_USER_ID);

        // Then — если исключение не выброшено, тест пройден
        verify(userRepository, times(1)).checkIfNotExists(EXISTING_USER_ID);
    }

    @Test
    void checkThatUserExists_NonExistingUser_ShouldThrowNotFoundException() {
        // Given
        when(userRepository.checkIfNotExists(NON_EXISTING_USER_ID)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.checkThatUserExists(NON_EXISTING_USER_ID))
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
        User updatedUser = new User(userId, "newemail@example.com", "User Name");
        UserResponseDTO expectedResponse = UserMapper.mapToResponseDTO(updatedUser);

        when(userRepository.get(userId)).thenReturn(existingUser);
        when(userRepository.checkIfNotExists(userId)).thenReturn(false);
        when(userRepository.existsByEmail(updateDTO.email())).thenReturn(false);
        when(userRepository.update(existingUser)).thenReturn(updatedUser);

        // When
        UserResponseDTO result = userService.update(userId, updateDTO);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(existingUser.getEmail()).isEqualTo("newemail@example.com");
        assertThat(existingUser.getName()).isEqualTo("User Name"); // не изменилось
        verify(userRepository, times(1)).update(existingUser);
    }

    @Test
    void update_OnlyName_ShouldUpdateName() {
        // Given
        Long userId = 1L;
        UserUpdateDTO updateDTO = new UserUpdateDTO(null, "New Name");
        User existingUser = new User(userId, "email@example.com", "Old Name");
        User updatedUser = new User(userId, "email@example.com", "New Name");
        UserResponseDTO expectedResponse = UserMapper.mapToResponseDTO(updatedUser);

        when(userRepository.get(userId)).thenReturn(existingUser);
        when(userRepository.checkIfNotExists(userId)).thenReturn(false);
        when(userRepository.update(existingUser)).thenReturn(updatedUser);

        // When
        UserResponseDTO result = userService.update(userId, updateDTO);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(existingUser.getEmail()).isEqualTo("email@example.com"); // не изменилось
        assertThat(existingUser.getName()).isEqualTo("New Name");
        verify(userRepository, times(1)).update(existingUser);
    }
}
