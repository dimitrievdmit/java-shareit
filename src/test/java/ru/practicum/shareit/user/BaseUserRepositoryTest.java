package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
abstract class BaseUserRepositoryTest {

    protected UserRepository userRepository;
    protected final String email1 = "user1@test.com";
    protected final String email2 = "user2@test.com";
    protected final String name1 = "User One";
    protected final String name2 = "User Two";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void create_ValidUser_ShouldReturnUserWithGeneratedId() {
        // Given
        User user = new User(null, email1, name1);

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertNotNull(savedUser.getId());
        assertEquals(email1, savedUser.getEmail());
        assertEquals(name1, savedUser.getName());
    }

    @Test
    void get_ExistingUser_ShouldReturnUser() {
        // Given
        User user = new User(null, email1, name1);
        User savedUser = userRepository.save(user);
        Long userId = savedUser.getId();

        // When
        User foundUser = userRepository.findById(userId).orElse(null);

        // Then
        assertNotNull(foundUser);
        assertEquals(userId, foundUser.getId());
        assertEquals(email1, foundUser.getEmail());
        assertEquals(name1, foundUser.getName());
    }

    @Test
    void get_NonExistingUser_ShouldReturnNull() {
        // When
        User user = userRepository.findById(999L).orElse(null);

        // Then
        assertNull(user);
    }

    @Test
    void update_ExistingUser_ShouldUpdateUser() {
        // Given
        User user = new User(null, email1, name1);
        User savedUser = userRepository.save(user);
        Long userId = savedUser.getId();

        // Создаём обновлённую версию
        User updatedUser = new User(userId, "newemail@test.com", "New Name");

        // When
        User result = userRepository.save(updatedUser);

        // Then
        assertEquals(userId, result.getId());
        assertEquals("newemail@test.com", result.getEmail());
        assertEquals("New Name", result.getName());
    }

    @Test
    void delete_ExistingUser_ShouldRemoveUser() {
        // Given
        User user = new User(null, email1, name1);
        User savedUser = userRepository.save(user);
        Long userId = savedUser.getId();

        // When
        userRepository.deleteById(userId);

        // Then: пользователь не должен находиться после удаления
        assertNull(userRepository.findById(userId).orElse(null));
        assertFalse(userRepository.existsById(userId));
    }

    @Test
    void delete_NonExistingUser_ShouldNotThrowException() {
        // When & Then: удаление несуществующего пользователя не должно вызывать исключений
        assertDoesNotThrow(() -> userRepository.deleteById(999L));
    }

    @Test
    void notExists_ById_ExistingUser_ShouldReturnFalse() {
        // Given
        User user = new User(null, email1, name1);
        User savedUser = userRepository.save(user);

        // When
        boolean exists = userRepository.existsById(savedUser.getId());

        // Then
        assertTrue(exists);
    }

    @Test
    void notExists_ById_NonExistingUser_ShouldReturnTrue() {
        // When
        boolean exists = userRepository.existsById(999L);

        // Then
        assertFalse(exists);
    }

    @Test
    void existsByEmail_ExistingEmail_ShouldReturnTrue() {
        // Given
        User user = new User(null, email1, name1);
        userRepository.save(user);

        // When
        boolean exists = userRepository.existsByEmailIgnoreCase(email1);

        // Then
        assertTrue(exists);
    }

    @Test
    void existsByEmail_NonExistingEmail_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByEmailIgnoreCase("nonexistent@test.com");

        // Then
        assertFalse(exists);
    }

    @Test
    void existsByEmail_EmptyEmail_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByEmailIgnoreCase("");

        // Then
        assertFalse(exists);
    }

    @Test
    void existsByEmail_NullEmail_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByEmailIgnoreCase(null);

        // Then
        assertFalse(exists);
    }

    @Test
    void existsByEmail_CaseInsensitive_ShouldReturnTrue() {
        // Given: создаём пользователя с email в нижнем регистре
        User user = new User(null, "test@test.com", name1);
        userRepository.save(user);

        // When: ищем с email в верхнем регистре
        boolean exists = userRepository.existsByEmailIgnoreCase("TEST@TEST.COM");

        // Then: должно найти, т.к. поиск не чувствителен к регистру
        assertTrue(exists);
    }

    @Test
    void create_MultipleUsers_ShouldGenerateUniqueIds() {
        // Given & When: создаём нескольких пользователей
        User user1 = new User(null, email1, name1);
        User user2 = new User(null, email2, name2);

        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);

        // Then: ID должны быть уникальными и последовательно возрастающими
        assertNotNull(savedUser1.getId());
        assertNotNull(savedUser2.getId());
        assertNotEquals(savedUser1.getId(), savedUser2.getId());
        assertTrue(savedUser2.getId() > savedUser1.getId());
    }
}
