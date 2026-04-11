package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.PermissionException;
import ru.practicum.shareit.item.dto.item.ItemCreateDTO;
import ru.practicum.shareit.item.dto.item.ItemResponseDTO;
import ru.practicum.shareit.item.dto.item.ItemUpdateDTO;
import ru.practicum.shareit.item.dto.item.ItemWithBookingDTO;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@Transactional
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private BookingRepository bookingRepository; // мок, чтобы не поднимать бронирования

    @MockBean
    private UserService userService; // мок проверки пользователей

    private User owner;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
        owner = userRepository.save(new User(null, "Owner", "owner@test.com"));
        anotherUser = userRepository.save(new User(null, "Another", "another@test.com"));
        doNothing().when(userService).isExistsOrElseThrow(owner.getId());
        doNothing().when(userService).isExistsOrElseThrow(anotherUser.getId());
    }

    @Test
    void create_ShouldSaveItem() {
        ItemCreateDTO dto = new ItemCreateDTO("Дрель", "Аккумуляторная", true, null);
        ItemResponseDTO created = itemService.create(dto, owner.getId());
        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("Дрель");
        assertThat(itemRepository.existsById(created.id())).isTrue();
    }

    @Test
    void get_ShouldReturnItemForOwner() {
        Item item = itemRepository.save(new Item(null, owner.getId(), "Вещь", "Описание", true, null));
        ItemWithBookingDTO found = itemService.get(item.getId(), owner.getId());
        assertThat(found.id()).isEqualTo(item.getId());
        assertThat(found.name()).isEqualTo("Вещь");
    }

    @Test
    void getAllByOwner_ShouldReturnOnlyOwnerItems() {
        itemRepository.save(new Item(null, owner.getId(), "Item1", "Desc1", true, null));
        itemRepository.save(new Item(null, owner.getId(), "Item2", "Desc2", false, null));
        itemRepository.save(new Item(null, anotherUser.getId(), "Item3", "Desc3", true, null));

        Collection<ItemWithBookingDTO> items = itemService.getAllByOwner(owner.getId());
        assertThat(items).hasSize(2);
        assertThat(items).extracting(ItemWithBookingDTO::name).containsExactlyInAnyOrder("Item1", "Item2");
    }

    @Test
    void search_ShouldReturnAvailableMatchingItems() {
        itemRepository.save(new Item(null, owner.getId(), "Phone", "Smartphone", true, null));
        itemRepository.save(new Item(null, owner.getId(), "Laptop", "Gaming laptop", true, null));
        itemRepository.save(new Item(null, owner.getId(), "Old Phone", "Old phone", false, null));

        Collection<ItemResponseDTO> result = itemService.search("phone");
        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().name()).isEqualTo("Phone");
    }

    @Test
    void update_ShouldModifyItem() {
        Item item = itemRepository.save(new Item(null, owner.getId(), "Old Name", "Old Desc", true, null));
        ItemUpdateDTO updateDto = new ItemUpdateDTO("New Name", null, false, null);
        ItemResponseDTO updated = itemService.update(item.getId(), owner.getId(), updateDto);
        assertThat(updated.name()).isEqualTo("New Name");
        assertThat(updated.available()).isFalse();

        Item fromDb = itemRepository.findById(item.getId()).orElseThrow();
        assertThat(fromDb.getName()).isEqualTo("New Name");
        assertThat(fromDb.getAvailable()).isFalse();
    }

    @Test
    void delete_ShouldRemoveItem() {
        Item item = itemRepository.save(new Item(null, owner.getId(), "To Delete", "Desc", true, null));
        itemService.delete(item.getId(), owner.getId());
        assertThat(itemRepository.existsById(item.getId())).isFalse();
    }

    @Test
    void update_ByNonOwner_ShouldThrowPermissionException() {
        Item item = itemRepository.save(new Item(null, owner.getId(), "Item", "Desc", true, null));
        ItemUpdateDTO updateDto = new ItemUpdateDTO("Hacked", null, null, null);
        assertThatThrownBy(() -> itemService.update(item.getId(), anotherUser.getId(), updateDto))
                .isInstanceOf(PermissionException.class);
    }

    @Test
    void get_NonExistentItem_ShouldThrowNotFoundException() {
        assertThatThrownBy(() -> itemService.get(999L, owner.getId()))
                .isInstanceOf(NotFoundException.class);
    }
}