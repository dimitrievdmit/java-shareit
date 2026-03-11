package ru.yandex.practicum.shareit.item;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class ItemRepositoryImplTest extends BaseItemRepositoryTest {

    @Override
    protected ItemRepository createItemRepository() {
        return new ItemRepositoryImpl();
    }
}
