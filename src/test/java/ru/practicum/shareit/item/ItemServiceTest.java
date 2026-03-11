package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.PermissionException;
import ru.practicum.shareit.item.dto.ItemCreateDTO;
import ru.practicum.shareit.item.dto.ItemResponseDTO;
import ru.practicum.shareit.item.dto.ItemUpdateDTO;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.service.UserService;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ItemServiceImpl itemService;

    private final Long EXISTING_ITEM_ID = 1L;
    private final Long NON_EXISTING_ITEM_ID = 999L;
    private final Long OWNER_ID = 100L;
    private final Long ANOTHER_OWNER_ID = 200L;

    // --- Тесты для create() ---

    @Test
    void create_ValidData_ShouldCreateItem() {
        // Given
        ItemCreateDTO createDTO = new ItemCreateDTO("New Item", "Description", true, 1L);
        Item expectedItem = new Item(1L, OWNER_ID, "New Item", "Description", true, 1L);
        ItemResponseDTO expectedResponse = ItemMapper.mapToResponseDTO(expectedItem);

        doNothing().when(userService).checkThatUserExists(OWNER_ID);
        when(itemRepository.create(any(Item.class))).thenReturn(expectedItem);

        // When
        ItemResponseDTO result = itemService.create(createDTO, OWNER_ID);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        verify(userService, times(1)).checkThatUserExists(OWNER_ID);
        verify(itemRepository, times(1)).create(any(Item.class));
    }

    @Test
    void create_OwnerDoesNotExist_ShouldThrowNotFoundException() {
        // Given
        ItemCreateDTO createDTO = new ItemCreateDTO("New Item", "Description", true, 1L);

        doThrow(new NotFoundException("Пользователь с id = " + OWNER_ID + " не найден"))
                .when(userService).checkThatUserExists(OWNER_ID);

        // When & Then
        assertThatThrownBy(() -> itemService.create(createDTO, OWNER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = " + OWNER_ID + " не найден");
    }

    // --- Тесты для get() ---

    @Test
    void get_ExistingItem_ShouldReturnItem() {
        // Given
        Item existingItem = new Item(EXISTING_ITEM_ID, OWNER_ID, "Item Name", "Description", true, 1L);
        ItemResponseDTO expectedResponse = ItemMapper.mapToResponseDTO(existingItem);

        when(itemRepository.get(EXISTING_ITEM_ID)).thenReturn(existingItem);
        when(itemRepository.checkIfNotExists(EXISTING_ITEM_ID)).thenReturn(false);

        // When
        ItemResponseDTO result = itemService.get(EXISTING_ITEM_ID);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        verify(itemRepository).checkIfNotExists(EXISTING_ITEM_ID);
    }

    @Test
    void get_NonExistingItem_ShouldThrowNotFoundException() {
        // Given
        when(itemRepository.checkIfNotExists(NON_EXISTING_ITEM_ID)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> itemService.get(NON_EXISTING_ITEM_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Вещь с ID = " + NON_EXISTING_ITEM_ID + " не найдена");
    }

    // --- Тесты для getAllByOwner() ---

    @Test
    void getAllByOwner_ExistingOwner_ShouldReturnItems() {
        // Given
        List<Item> ownerItems = Arrays.asList(
                new Item(1L, OWNER_ID, "Item 1", "Desc 1", true, 1L),
                new Item(2L, OWNER_ID, "Item 2", "Desc 2", false, 2L)
        );
        List<ItemResponseDTO> expectedResponses = ItemMapper.mapToResponseDTOList(ownerItems);

        doNothing().when(userService).checkThatUserExists(OWNER_ID);
        when(itemRepository.getAllByOwner(OWNER_ID)).thenReturn(ownerItems);

        // When
        Collection<ItemResponseDTO> result = itemService.getAllByOwner(OWNER_ID);

        // Then
        assertThat(result).containsExactlyElementsOf(expectedResponses);
    }

    @Test
    void getAllByOwner_OwnerDoesNotExist_ShouldThrowNotFoundException() {
        // Given
        doThrow(new NotFoundException("Пользователь с id = " + OWNER_ID + " не найден"))
                .when(userService).checkThatUserExists(OWNER_ID);

        // When & Then
        assertThatThrownBy(() -> itemService.getAllByOwner(OWNER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = " + OWNER_ID + " не найден");
    }

    // --- Тесты для search() ---

    @Test
    void search_WithValidText_ShouldReturnMatchingItems() {
        // Given
        String searchText = "test";
        List<Item> foundItems = List.of(new Item(1L, OWNER_ID, "Test Item", "Test Description", true, 1L));
        List<ItemResponseDTO> expectedResponses = ItemMapper.mapToResponseDTOList(foundItems);

        when(itemRepository.search(searchText)).thenReturn(foundItems);

        // When
        Collection<ItemResponseDTO> result = itemService.search(searchText);

        // Then
        assertThat(result).containsExactlyElementsOf(expectedResponses);
    }

    @Test
    void search_WithEmptyText_ShouldReturnEmptyList() {
        // Given
        String emptyText = "";
        when(itemRepository.search(emptyText)).thenReturn(Collections.emptyList());

        // When
        Collection<ItemResponseDTO> result = itemService.search(emptyText);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void search_WithNullText_ShouldReturnEmptyList() {
        // Given
        when(itemRepository.search(null)).thenReturn(Collections.emptyList());

        // When
        Collection<ItemResponseDTO> result = itemService.search(null);

        // Then
        assertThat(result).isEmpty();
    }

    // --- Тесты для update() ---
    @Test
    void update_ExistingItemByOwner_ShouldUpdateItem() {
        // Given
        Long itemId = 1L;
        ItemUpdateDTO updateDTO = new ItemUpdateDTO("Updated Name", "Updated Description", false, 2L);
        Item existingItem = new Item(itemId, OWNER_ID, "Old Name", "Old Description", true, 1L);
        Item updatedItem = new Item(itemId, OWNER_ID, "Updated Name", "Updated Description", false, 2L);
        ItemResponseDTO expectedResponse = ItemMapper.mapToResponseDTO(updatedItem);

        when(itemRepository.get(itemId)).thenReturn(existingItem);
        when(itemRepository.checkIfNotExists(itemId)).thenReturn(false);
        doNothing().when(userService).checkThatUserExists(OWNER_ID);
        when(itemRepository.update(existingItem)).thenReturn(updatedItem);

        // When
        ItemResponseDTO result = itemService.update(itemId, OWNER_ID, updateDTO);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        verify(itemRepository, times(1)).update(existingItem);
        assertThat(existingItem.getName()).isEqualTo("Updated Name");
        assertThat(existingItem.getDescription()).isEqualTo("Updated Description");
        assertThat(existingItem.getAvailable()).isFalse();
        assertThat(existingItem.getRequestId()).isEqualTo(2L);
    }

    @Test
    void update_PartialUpdateWithOnlyName_ShouldUpdateOnlyName() {
        // Given
        Long itemId = 1L;
        ItemUpdateDTO partialUpdate = new ItemUpdateDTO("New Name", null, null, null);
        Item existingItem = new Item(itemId, OWNER_ID, "Old Name", "Description", true, 1L);
        Item updatedItem = new Item(itemId, OWNER_ID, "New Name", "Description", true, 1L);
        ItemResponseDTO expectedResponse = ItemMapper.mapToResponseDTO(updatedItem);

        when(itemRepository.get(itemId)).thenReturn(existingItem);
        when(itemRepository.checkIfNotExists(itemId)).thenReturn(false);
        doNothing().when(userService).checkThatUserExists(OWNER_ID);
        when(itemRepository.update(existingItem)).thenReturn(updatedItem);

        // When
        ItemResponseDTO result = itemService.update(itemId, OWNER_ID, partialUpdate);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(existingItem.getName()).isEqualTo("New Name");
        assertThat(existingItem.getDescription()).isEqualTo("Description"); // не изменилось
        assertThat(existingItem.getAvailable()).isTrue(); // не изменилось
        assertThat(existingItem.getRequestId()).isEqualTo(1L); // не изменилось
    }

    @Test
    void update_NonExistingItem_ShouldThrowNotFoundException() {
        // Given
        when(itemRepository.checkIfNotExists(NON_EXISTING_ITEM_ID)).thenReturn(true);

        ItemUpdateDTO updateDTO = new ItemUpdateDTO("Updated Name", "Updated Description", false, 2L);

        // When & Then
        assertThatThrownBy(() -> itemService.update(NON_EXISTING_ITEM_ID, OWNER_ID, updateDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Вещь с ID = " + NON_EXISTING_ITEM_ID + " не найдена");
    }

    @Test
    void update_OwnerDoesNotExist_ShouldThrowNotFoundException() {
        // Given
        Long itemId = 1L;
        ItemUpdateDTO updateDTO = new ItemUpdateDTO("Updated Name", "Updated Description", false, 2L);

        when(itemRepository.checkIfNotExists(itemId)).thenReturn(false);
        doThrow(new NotFoundException("Пользователь с id = " + OWNER_ID + " не найден"))
                .when(userService).checkThatUserExists(OWNER_ID);

        // When & Then
        assertThatThrownBy(() -> itemService.update(itemId, OWNER_ID, updateDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = " + OWNER_ID + " не найден");
    }

    @Test
    void update_AnotherOwnerTriesToUpdate_ShouldThrowPermissionException() {
        // Given
        Long itemId = 1L;
        ItemUpdateDTO updateDTO = new ItemUpdateDTO("Updated Name", "Updated Description", false, 2L);
        Item existingItem = new Item(itemId, OWNER_ID, "Old Name", "Old Description", true, 1L);

        when(itemRepository.get(itemId)).thenReturn(existingItem);
        when(itemRepository.checkIfNotExists(itemId)).thenReturn(false);
        doNothing().when(userService).checkThatUserExists(ANOTHER_OWNER_ID);

        // When & Then
        assertThatThrownBy(() -> itemService.update(itemId, ANOTHER_OWNER_ID, updateDTO))
                .isInstanceOf(PermissionException.class)
                .hasMessageContaining("Владелец с ID " + ANOTHER_OWNER_ID + " не имеет прав на выполнение операции с вещью с ID " + itemId);
    }

    // --- Тесты для delete() ---


    @Test
    void delete_ExistingItemByOwner_ShouldDeleteItem() {
        // Given
        Long itemId = 1L;
        Item existingItem = new Item(itemId, OWNER_ID, "Item Name", "Description", true, 1L);

        when(itemRepository.get(itemId)).thenReturn(existingItem);
        when(itemRepository.checkIfNotExists(EXISTING_ITEM_ID)).thenReturn(false);
        doNothing().when(userService).checkThatUserExists(OWNER_ID);

        // When
        itemService.delete(EXISTING_ITEM_ID, OWNER_ID);

        // Then
        verify(itemRepository, times(1)).delete(EXISTING_ITEM_ID);
    }

    @Test
    void delete_NonExistingItem_ShouldThrowNotFoundException() {
        // Given
        when(itemRepository.checkIfNotExists(NON_EXISTING_ITEM_ID)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> itemService.delete(NON_EXISTING_ITEM_ID, OWNER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Вещь с ID = " + NON_EXISTING_ITEM_ID + " не найдена");
    }

    @Test
    void delete_OwnerDoesNotExist_ShouldThrowNotFoundException() {
        // Given
        when(itemRepository.checkIfNotExists(EXISTING_ITEM_ID)).thenReturn(false);
        doThrow(new NotFoundException("Пользователь с id = " + OWNER_ID + " не найден"))
                .when(userService).checkThatUserExists(OWNER_ID);

        // When & Then
        assertThatThrownBy(() -> itemService.delete(EXISTING_ITEM_ID, OWNER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = " + OWNER_ID + " не найден");
    }
    @Test
    void delete_AnotherOwnerTriesToDelete_ShouldThrowPermissionException() {
        // Given
        Long itemId = 1L;
        Item existingItem = new Item(itemId, OWNER_ID, "Item Name", "Description", true, 1L);

        when(itemRepository.get(itemId)).thenReturn(existingItem);
        when(itemRepository.checkIfNotExists(itemId)).thenReturn(false);
        doNothing().when(userService).checkThatUserExists(ANOTHER_OWNER_ID);

        // When & Then
        assertThatThrownBy(() -> itemService.delete(itemId, ANOTHER_OWNER_ID))
                .isInstanceOf(PermissionException.class)
                .hasMessageContaining("Владелец с ID " + ANOTHER_OWNER_ID + " не имеет прав на выполнение операции с вещью с ID " + itemId);

        // Then — проверяем, что удаление не было выполнено
        verify(itemRepository, never()).delete(itemId);
    }

    // --- Тесты для checkThatItemExists() ---

    @Test
    void checkThatItemExists_ExistingItem_ShouldNotThrowException() {
        // Given
        when(itemRepository.checkIfNotExists(EXISTING_ITEM_ID)).thenReturn(false);

        // When
        itemService.checkThatItemExists(EXISTING_ITEM_ID);

        // Then — если исключение не выброшено, тест пройден
    }

    @Test
    void checkThatItemExists_NonExistingItem_ShouldThrowNotFoundException() {
        // Given
        when(itemRepository.checkIfNotExists(NON_EXISTING_ITEM_ID)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> itemService.checkThatItemExists(NON_EXISTING_ITEM_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Вещь с ID = " + NON_EXISTING_ITEM_ID + " не найдена");
    }

    // --- Дополнительные тесты валидации бизнес‑логики ---
}
