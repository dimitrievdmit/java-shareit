package ru.practicum.shareit.user;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.repository.UserRepositoryImpl;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest extends BaseUserRepositoryTest {

    @Override
    protected UserRepository createUserRepository() {
        return new UserRepositoryImpl();
    }
}
