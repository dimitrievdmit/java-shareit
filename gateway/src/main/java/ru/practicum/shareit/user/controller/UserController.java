package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserCreateDTO;
import ru.practicum.shareit.user.dto.UserUpdateDTO;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserClient userClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        return userClient.create(userCreateDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable @Positive(message = "Ид должен быть больше 0") Long id) {
        return userClient.get(id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(
            @PathVariable @Positive(message = "Ид должен быть больше 0") Long id,
            @Valid @RequestBody UserUpdateDTO userUpdateDTO
    ) {
        return userClient.update(id, userUpdateDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Object> delete(@PathVariable @Positive(message = "Ид должен быть больше 0") Long id) {
        return userClient.delete(id);
    }
}