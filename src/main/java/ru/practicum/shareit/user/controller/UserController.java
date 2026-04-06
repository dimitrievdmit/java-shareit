package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserCreateDTO;
import ru.practicum.shareit.user.dto.UserResponseDTO;
import ru.practicum.shareit.user.dto.UserUpdateDTO;
import ru.practicum.shareit.user.service.UserService;


@SuppressWarnings("unused")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO create(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        return userService.create(userCreateDTO);
    }

    @GetMapping("/{id}")
    public UserResponseDTO get(@PathVariable @Positive(message = "Ид должен быть больше 0") Long id) {
        return userService.get(id);
    }

    @PatchMapping("/{id}")
    public UserResponseDTO update(
            @PathVariable @Positive(message = "Ид должен быть больше 0") Long id,
            @Valid @RequestBody UserUpdateDTO userUpdateDTO
    ) {
        return userService.update(id, userUpdateDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive(message = "Ид должен быть больше 0") Long id) {
        userService.delete(id);
    }
}
