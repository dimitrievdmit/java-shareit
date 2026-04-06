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
        User user = new User(null, name1, email1);
        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals(name1, savedUser.getName());
        assertEquals(email1, savedUser.getEmail());
    }

    @Test
    void get_ExistingUser_ShouldReturnUser() {
        User user = new User(null, name1, email1);
        User savedUser = userRepository.save(user);
        Long userId = savedUser.getId();

        User foundUser = userRepository.findById(userId).orElse(null);

        assertNotNull(foundUser);
        assertEquals(userId, foundUser.getId());
        assertEquals(name1, foundUser.getName());
        assertEquals(email1, foundUser.getEmail());
    }

    @Test
    void get_NonExistingUser_ShouldReturnNull() {
        User user = userRepository.findById(999L).orElse(null);
        assertNull(user);
    }

    @Test
    void update_ExistingUser_ShouldUpdateUser() {
        User user = new User(null, name1, email1);
        User savedUser = userRepository.save(user);
        Long userId = savedUser.getId();

        User updatedUser = new User(userId, "New Name", "newemail@test.com");
        User result = userRepository.save(updatedUser);

        assertEquals(userId, result.getId());
        assertEquals("New Name", result.getName());
        assertEquals("newemail@test.com", result.getEmail());
    }

    @Test
    void delete_ExistingUser_ShouldRemoveUser() {
        User user = new User(null, name1, email1);
        User savedUser = userRepository.save(user);
        Long userId = savedUser.getId();

        userRepository.deleteById(userId);

        assertNull(userRepository.findById(userId).orElse(null));
        assertFalse(userRepository.existsById(userId));
    }

    @Test
    void delete_NonExistingUser_ShouldNotThrowException() {
        assertDoesNotThrow(() -> userRepository.deleteById(999L));
    }

    @Test
    void notExists_ById_ExistingUser_ShouldReturnFalse() {
        User user = new User(null, name1, email1);
        User savedUser = userRepository.save(user);

        boolean exists = userRepository.existsById(savedUser.getId());
        assertTrue(exists);
    }

    @Test
    void notExists_ById_NonExistingUser_ShouldReturnTrue() {
        boolean exists = userRepository.existsById(999L);
        assertFalse(exists);
    }

    @Test
    void existsByEmail_ExistingEmail_ShouldReturnTrue() {
        User user = new User(null, name1, email1);
        userRepository.save(user);

        boolean exists = userRepository.existsByEmailIgnoreCase(email1);
        assertTrue(exists);
    }

    @Test
    void existsByEmail_NonExistingEmail_ShouldReturnFalse() {
        boolean exists = userRepository.existsByEmailIgnoreCase("nonexistent@test.com");
        assertFalse(exists);
    }

    @Test
    void existsByEmail_EmptyEmail_ShouldReturnFalse() {
        boolean exists = userRepository.existsByEmailIgnoreCase("");
        assertFalse(exists);
    }

    @Test
    void existsByEmail_NullEmail_ShouldReturnFalse() {
        boolean exists = userRepository.existsByEmailIgnoreCase(null);
        assertFalse(exists);
    }

    @Test
    void existsByEmail_CaseInsensitive_ShouldReturnTrue() {
        User user = new User(null, name1, "test@test.com");
        userRepository.save(user);

        boolean exists = userRepository.existsByEmailIgnoreCase("TEST@TEST.COM");
        assertTrue(exists);
    }

    @Test
    void create_MultipleUsers_ShouldGenerateUniqueIds() {
        User user1 = new User(null, name1, email1);
        User user2 = new User(null, name2, email2);

        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);

        assertNotNull(savedUser1.getId());
        assertNotNull(savedUser2.getId());
        assertNotEquals(savedUser1.getId(), savedUser2.getId());
        assertTrue(savedUser2.getId() > savedUser1.getId());
    }
}