package ru.practicum.shareit.user.repository;


import ru.practicum.shareit.user.model.User;

public interface UserRepository {

    User create(User user);

    User get(Long id);

    User update(User newUser);

    void delete(Long id);

    boolean checkIfNotExists(Long id);

    boolean existsByEmail(String email);
}
