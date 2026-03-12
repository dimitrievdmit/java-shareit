package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unused"})
@Slf4j
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1L;

    @Override
    public User create(User user) {
        Long newId = nextId++;
        User newUser = new User(newId, user.email(), user.name());
        users.put(newId, newUser);
        log.info("Создан пользователь с ID {}: {}", newId, newUser.name());
        return newUser;
    }

    @Override
    public User get(Long id) {
        return users.get(id);
    }

    @Override
    public User update(User newUser) {
        Long userId = newUser.id();
        users.put(userId, newUser);
        log.info("Обновлён пользователь с ID {}: {}", userId, newUser.name());
        return newUser;
    }

    @Override
    public void delete(Long id) {
        if (users.remove(id) != null) {
            log.info("Удален пользователь с ID {}", id);
        } else {
            log.warn("Попытка удаления несуществующего пользователя с ID {}", id);
        }
    }

    @Override
    public boolean checkIfNotExists(Long id) {
        boolean notExists = !users.containsKey(id);
        if (notExists) {
            log.warn("Пользователь с ID {} не найден в хранилище", id);
        }
        return notExists;
    }

    @Override
    public boolean existsByEmail(String email) {
        return users.values().stream()
                .anyMatch(user -> user.email().equalsIgnoreCase(email));
    }
}
