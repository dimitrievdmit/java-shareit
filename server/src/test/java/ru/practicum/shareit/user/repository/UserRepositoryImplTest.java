package ru.practicum.shareit.user.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRepositoryImplTest extends BaseUserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    @BeforeEach
    void setUp() {
        super.userRepository = userRepository;
        entityManager.clear();
    }

    @Transactional
    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        entityManager.clear();
    }
}
