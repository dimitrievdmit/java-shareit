package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.dto.UserCreateDTO;
import ru.practicum.shareit.user.dto.UserResponseDTO;
import ru.practicum.shareit.user.dto.UserUpdateDTO;

import static ru.practicum.shareit.user.mapper.UserMapper.mapToDomain;
import static ru.practicum.shareit.user.mapper.UserMapper.mapToResponseDTO;


@SuppressWarnings("unused")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO create(@Valid @RequestBody UserCreateDTO user) {
        return mapToResponseDTO(userService.create(mapToDomain(user)));
    }

    @GetMapping("/{id}")
    public UserResponseDTO get(@PathVariable @Positive(message = "Ид должен быть больше 0") Long id) {
        return mapToResponseDTO(userService.get(id));
    }

    @PatchMapping("/{id}")
    public UserResponseDTO update(
            @PathVariable @Positive(message = "Ид должен быть больше 0") Long id,
            @Valid @RequestBody UserUpdateDTO newUser
    ) {
        return mapToResponseDTO(userService.update(id, mapToDomain(newUser)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive(message = "Ид должен быть больше 0") Long id) {
        userService.delete(id);
    }
}
