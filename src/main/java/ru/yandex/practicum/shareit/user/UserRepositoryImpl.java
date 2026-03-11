package ru.yandex.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

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
        User newUser = new User(newId, user.getEmail(), user.getName());
        users.put(newId, newUser);
        log.info("Создан пользователь с ID {}: {}", newId, newUser.getName());
        return newUser;
    }

    @Override
    public User get(Long id) {
        return users.get(id);
    }

    @Override
    public User update(User newUser) {
        Long userId = newUser.getId();
        users.put(userId, newUser);
        log.info("Обновлён пользователь с ID {}: {}", userId, newUser.getName());
        return newUser;
    }

    @Override
    public void delete(Long id) {
        if (users.remove(id) != null) {
            log.info("Удален пользователь с ID {}", id);
        } else {
            log.debug("Попытка удаления несуществующего пользователя с ID {}", id);
        }
    }

    @Override
    public boolean checkIfNotExists(Long id) {
        boolean notExists = !users.containsKey(id);
        if (notExists) {
            log.debug("Пользователь с ID {} не найден в хранилище", id);
        }
        return notExists;
    }

    @Override
    public boolean existsByEmail(String email) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }
}
