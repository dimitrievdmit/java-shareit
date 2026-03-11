package ru.yandex.practicum.shareit.user;


public interface UserRepository {

    User create(User user);

    User get(Long id);

    User update(User newUser);

    void delete(Long id);

    boolean checkIfNotExists(Long id);

    boolean existsByEmail(String email);
}
