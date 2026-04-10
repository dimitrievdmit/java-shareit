package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserCreateDTO;
import ru.practicum.shareit.user.dto.UserResponseDTO;
import ru.practicum.shareit.user.dto.UserUpdateDTO;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    // --- create ---
    @Test
    void create_ValidData_ShouldSaveUser() {
        UserCreateDTO dto = new UserCreateDTO("Integration User", "integration@test.com");
        UserResponseDTO created = userService.create(dto);
        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("Integration User");
        assertThat(created.email()).isEqualTo("integration@test.com");

        User fromDb = userRepository.findById(created.id()).orElseThrow();
        assertThat(fromDb.getName()).isEqualTo("Integration User");
    }

    @Test
    void create_DuplicateEmail_ShouldThrowAlreadyExistException() {
        UserCreateDTO dto1 = new UserCreateDTO("User1", "dup@test.com");
        UserCreateDTO dto2 = new UserCreateDTO("User2", "dup@test.com");
        userService.create(dto1);
        assertThatThrownBy(() -> userService.create(dto2))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessageContaining("Пользователь с email 'dup@test.com' уже существует");
    }

    // --- get ---
    @Test
    void get_ExistingId_ShouldReturnUser() {
        UserCreateDTO dto = new UserCreateDTO("Get User", "get@test.com");
        UserResponseDTO created = userService.create(dto);
        UserResponseDTO found = userService.get(created.id());
        assertThat(found.id()).isEqualTo(created.id());
        assertThat(found.email()).isEqualTo("get@test.com");
    }

    @Test
    void get_NonExistingId_ShouldThrowNotFoundException() {
        assertThatThrownBy(() -> userService.get(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = 999 не найден");
    }

    // --- update ---
    @Test
    void update_ChangeNameAndEmail_ShouldUpdate() {
        UserCreateDTO createDto = new UserCreateDTO("Before", "before@test.com");
        UserResponseDTO created = userService.create(createDto);
        UserUpdateDTO updateDto = new UserUpdateDTO("After", "after@test.com");
        UserResponseDTO updated = userService.update(created.id(), updateDto);
        assertThat(updated.name()).isEqualTo("After");
        assertThat(updated.email()).isEqualTo("after@test.com");

        User fromDb = userRepository.findById(created.id()).orElseThrow();
        assertThat(fromDb.getName()).isEqualTo("After");
        assertThat(fromDb.getEmail()).isEqualTo("after@test.com");
    }

    @Test
    void update_ChangeEmailToExisting_ShouldThrowAlreadyExistException() {
        UserCreateDTO dto1 = new UserCreateDTO("First", "first@test.com");
        UserCreateDTO dto2 = new UserCreateDTO("Second", "second@test.com");
        UserResponseDTO user1 = userService.create(dto1);
        userService.create(dto2);
        UserUpdateDTO updateDto = new UserUpdateDTO(null, "first@test.com"); // пытаемся занять email user1
        assertThatThrownBy(() -> userService.update(user1.id() + 1, updateDto))
                .isInstanceOf(AlreadyExistException.class);
    }

    @Test
    void update_NonExistingUser_ShouldThrowNotFoundException() {
        UserUpdateDTO updateDto = new UserUpdateDTO("No", "no@test.com");
        assertThatThrownBy(() -> userService.update(999L, updateDto))
                .isInstanceOf(NotFoundException.class);
    }

    // --- delete ---
    @Test
    void delete_ExistingUser_ShouldRemoveUser() {
        UserCreateDTO dto = new UserCreateDTO("ToDelete", "delete@test.com");
        UserResponseDTO created = userService.create(dto);
        userService.delete(created.id());
        assertThat(userRepository.existsById(created.id())).isFalse();
    }

    @Test
    void delete_NonExistingUser_ShouldThrowNotFoundException() {
        assertThatThrownBy(() -> userService.delete(999L))
                .isInstanceOf(NotFoundException.class);
    }

    // --- isExistsOrElseThrow ---
    @Test
    void isExistsOrElseThrow_ExistingId_ShouldNotThrow() {
        UserCreateDTO dto = new UserCreateDTO("Exists", "exists@test.com");
        UserResponseDTO created = userService.create(dto);
        userService.isExistsOrElseThrow(created.id()); // no exception
    }

    @Test
    void isExistsOrElseThrow_NonExistingId_ShouldThrowNotFoundException() {
        assertThatThrownBy(() -> userService.isExistsOrElseThrow(999L))
                .isInstanceOf(NotFoundException.class);
    }
}