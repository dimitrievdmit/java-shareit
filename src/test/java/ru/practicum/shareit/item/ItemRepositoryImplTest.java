package ru.practicum.shareit.item;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.repository.ItemRepositoryImpl;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class ItemRepositoryImplTest extends BaseItemRepositoryTest {

    @Override
    protected ItemRepository createItemRepository() {
        return new ItemRepositoryImpl();
    }
}
