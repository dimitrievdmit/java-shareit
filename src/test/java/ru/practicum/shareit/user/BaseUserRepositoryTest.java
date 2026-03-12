package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

abstract class BaseUserRepositoryTest {

    protected UserRepository userRepository;
    protected final String email1 = "user1@test.com";
    protected final String email2 = "user2@test.com";
    protected final String name1 = "User One";
    protected final String name2 = "User Two";

    @BeforeEach
    void setUp() {
        userRepository = createUserRepository();
    }

    /**
     * Фабричный метод для создания конкретной реализации репозитория.
     * Должен быть переопределён в имплементациях.
     */
    protected abstract UserRepository createUserRepository();

    @Test
    void create_ValidUser_ShouldReturnUserWithGeneratedId() {
        // Given
        User user = new User(null, email1, name1);

        // When
        User savedUser = userRepository.create(user);

        // Then
        assertNotNull(savedUser.id());
        assertEquals(email1, savedUser.email());
        assertEquals(name1, savedUser.name());
    }

    @Test
    void get_ExistingUser_ShouldReturnUser() {
        // Given
        User user = new User(null, email1, name1);
        User savedUser = userRepository.create(user);
        Long userId = savedUser.id();

        // When
        User foundUser = userRepository.get(userId);

        // Then
        assertNotNull(foundUser);
        assertEquals(userId, foundUser.id());
        assertEquals(email1, foundUser.email());
        assertEquals(name1, foundUser.name());
    }

    @Test
    void get_NonExistingUser_ShouldReturnNull() {
        // When
        User user = userRepository.get(999L);

        // Then
        assertNull(user);
    }

    @Test
    void update_ExistingUser_ShouldUpdateUser() {
        // Given
        User user = new User(null, email1, name1);
        User savedUser = userRepository.create(user);
        Long userId = savedUser.id();

        // Создаём обновлённую версию
        User updatedUser = new User(userId, "newemail@test.com", "New Name");

        // When
        User result = userRepository.update(updatedUser);

        // Then
        assertEquals(userId, result.id());
        assertEquals("newemail@test.com", result.email());
        assertEquals("New Name", result.name());
    }

    @Test
    void delete_ExistingUser_ShouldRemoveUser() {
        // Given
        User user = new User(null, email1, name1);
        User savedUser = userRepository.create(user);
        Long userId = savedUser.id();

        // When
        userRepository.delete(userId);

        // Then: пользователь не должен находиться после удаления
        assertNull(userRepository.get(userId));
        assertTrue(userRepository.checkIfNotExists(userId));
    }

    @Test
    void delete_NonExistingUser_ShouldNotThrowException() {
        // When & Then: удаление несуществующего пользователя не должно вызывать исключений
        assertDoesNotThrow(() -> userRepository.delete(999L));
    }

    @Test
    void checkIfNotExists_ExistingUser_ShouldReturnFalse() {
        // Given
        User user = new User(null, email1, name1);
        User savedUser = userRepository.create(user);

        // When
        boolean notExists = userRepository.checkIfNotExists(savedUser.id());

        // Then
        assertFalse(notExists);
    }

    @Test
    void checkIfNotExists_NonExistingUser_ShouldReturnTrue() {
        // When
        boolean notExists = userRepository.checkIfNotExists(999L);

        // Then
        assertTrue(notExists);
    }

    @Test
    void existsByEmail_ExistingEmail_ShouldReturnTrue() {
        // Given
        User user = new User(null, email1, name1);
        userRepository.create(user);

        // When
        boolean exists = userRepository.existsByEmail(email1);

        // Then
        assertTrue(exists);
    }

    @Test
    void existsByEmail_NonExistingEmail_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@test.com");

        // Then
        assertFalse(exists);
    }

    @Test
    void existsByEmail_EmptyEmail_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByEmail("");

        // Then
        assertFalse(exists);
    }

    @Test
    void existsByEmail_NullEmail_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByEmail(null);

        // Then
        assertFalse(exists);
    }

    @Test
    void existsByEmail_CaseInsensitive_ShouldReturnTrue() {
        // Given: создаём пользователя с email в нижнем регистре
        User user = new User(null, "test@test.com", name1);
        userRepository.create(user);

        // When: ищем с email в верхнем регистре
        boolean exists = userRepository.existsByEmail("TEST@TEST.COM");

        // Then: должно найти, т.к. поиск не чувствителен к регистру
        assertTrue(exists);
    }

    @Test
    void create_MultipleUsers_ShouldGenerateUniqueIds() {
        // Given & When: создаём нескольких пользователей
        User user1 = new User(null, email1, name1);
        User user2 = new User(null, email2, name2);

        User savedUser1 = userRepository.create(user1);
        User savedUser2 = userRepository.create(user2);

        // Then: ID должны быть уникальными и последовательно возрастающими
        assertNotNull(savedUser1.id());
        assertNotNull(savedUser2.id());
        assertNotEquals(savedUser1.id(), savedUser2.id());
        assertTrue(savedUser2.id() > savedUser1.id());
    }
}
