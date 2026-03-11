package ru.yandex.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@SuppressWarnings("unused")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO create(@Valid @RequestBody UserCreateDTO user) {
        return userService.create(user);
    }

    @GetMapping("/{id}")
    public UserResponseDTO get(@PathVariable @Positive(message = "Ид должен быть больше 0") Long id) {
        return userService.get(id);
    }

    @PatchMapping("/{id}")
    public UserResponseDTO update(
            @PathVariable @Positive(message = "Ид должен быть больше 0") Long id,
            @Valid @RequestBody UserUpdateDTO newUser
    ) {
        return userService.update(id, newUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive(message = "Ид должен быть больше 0") Long id) {
        userService.delete(id);
    }
}
