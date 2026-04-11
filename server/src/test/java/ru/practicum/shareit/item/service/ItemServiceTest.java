package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.LogicException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.PermissionException;
import ru.practicum.shareit.item.dto.comment.CommentResponseDTO;
import ru.practicum.shareit.item.dto.comment.CommentViewDTO;
import ru.practicum.shareit.item.dto.item.ItemCreateDTO;
import ru.practicum.shareit.item.dto.item.ItemResponseDTO;
import ru.practicum.shareit.item.dto.item.ItemUpdateDTO;
import ru.practicum.shareit.item.dto.item.ItemWithBookingDTO;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.validator.Validator;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ItemServiceImpl itemService;

    private static final Long EXISTING_ITEM_ID = 1L;
    private static final Long NON_EXISTING_ITEM_ID = 999L;
    private static final Long OWNER_ID = 100L;
    private static final Long ANOTHER_OWNER_ID = 200L;
    private static final Long USER_ID = 300L;

    private final LocalDateTime now = LocalDateTime.now();

    // --- Тесты для create() ---

    @Test
    void create_ValidData_ShouldCreateItem() {
        ItemCreateDTO createDTO = new ItemCreateDTO("New Item", "Description", true, 1L);
        Item expectedItem = new Item(1L, OWNER_ID, "New Item", "Description", true, 1L);
        ItemResponseDTO expectedResponse = ItemMapper.mapToResponseDTO(expectedItem);

        doNothing().when(userService).isExistsOrElseThrow(OWNER_ID);
        when(itemRepository.save(any(Item.class))).thenReturn(expectedItem);

        ItemResponseDTO result = itemService.create(createDTO, OWNER_ID);

        assertThat(result).isEqualTo(expectedResponse);
        verify(userService).isExistsOrElseThrow(OWNER_ID);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void create_OwnerDoesNotExist_ShouldThrowNotFoundException() {
        ItemCreateDTO createDTO = new ItemCreateDTO("New Item", "Description", true, 1L);
        doThrow(new NotFoundException("Пользователь с id = " + OWNER_ID + " не найден"))
                .when(userService).isExistsOrElseThrow(OWNER_ID);

        assertThatThrownBy(() -> itemService.create(createDTO, OWNER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = " + OWNER_ID + " не найден");
    }

    // --- Тесты для get() ---

    @Test
    void get_ExistingItem_ByOwner_ShouldReturnItemWithBookings() {
        Item item = new Item(EXISTING_ITEM_ID, OWNER_ID, "Item", "Desc", true, 1L);
        List<CommentResponseDTO> comments = List.of(new CommentResponseDTO(1L, "text", "author", now));
        List<Booking> bookings = List.of(
                createBooking(1L, now.minusDays(2), now.minusDays(1), BookingStatus.APPROVED, item),
                createBooking(2L, now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED, item)
        );

        when(itemRepository.findById(EXISTING_ITEM_ID)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemIdWithAuthor(EXISTING_ITEM_ID)).thenReturn(comments);
        when(bookingRepository.findByItemIdWithItem(EXISTING_ITEM_ID)).thenReturn(bookings);

        ItemWithBookingDTO result = itemService.get(EXISTING_ITEM_ID, OWNER_ID);

        assertThat(result.id()).isEqualTo(EXISTING_ITEM_ID);
        assertThat(result.ownerId()).isEqualTo(OWNER_ID);
        assertThat(result.lastBooking()).isNotNull();
        assertThat(result.nextBooking()).isNotNull();
        assertThat(result.comments()).hasSize(1);
        verify(bookingRepository).findByItemIdWithItem(EXISTING_ITEM_ID);
    }

    @Test
    void get_ExistingItem_ByNonOwner_ShouldReturnItemWithoutBookings() {
        Item item = new Item(EXISTING_ITEM_ID, OWNER_ID, "Item", "Desc", true, 1L);
        List<CommentResponseDTO> comments = List.of(new CommentResponseDTO(1L, "text", "author", now));

        when(itemRepository.findById(EXISTING_ITEM_ID)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemIdWithAuthor(EXISTING_ITEM_ID)).thenReturn(comments);

        ItemWithBookingDTO result = itemService.get(EXISTING_ITEM_ID, USER_ID);

        assertThat(result.id()).isEqualTo(EXISTING_ITEM_ID);
        assertThat(result.lastBooking()).isNull();
        assertThat(result.nextBooking()).isNull();
        assertThat(result.comments()).hasSize(1);
        verify(bookingRepository, never()).findByItemIdWithItem(anyLong());
    }

    @Test
    void get_NonExistingItem_ShouldThrowNotFoundException() {
        when(itemRepository.findById(NON_EXISTING_ITEM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.get(NON_EXISTING_ITEM_ID, USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Вещь с ID = " + NON_EXISTING_ITEM_ID + " не найдена");
    }

    // --- Тесты для getAllByOwner() ---

    @Test
    void getAllByOwner_ExistingOwner_ShouldReturnItemsWithBookingsAndComments() {
        Item item1 = new Item(1L, OWNER_ID, "Item1", "Desc1", true, 1L);
        Item item2 = new Item(2L, OWNER_ID, "Item2", "Desc2", false, null);
        List<Item> items = List.of(item1, item2);

        List<Booking> bookings = List.of(
                createBooking(10L, now.minusDays(3), now.minusDays(2), BookingStatus.APPROVED, item1),
                createBooking(11L, now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED, item1)
        );
        List<CommentViewDTO> commentViews = List.of(
                new CommentViewDTO(101L, "comment1", 1L, "User1", now),
                new CommentViewDTO(102L, "comment2", 2L, "User2", now)
        );

        doNothing().when(userService).isExistsOrElseThrow(OWNER_ID);
        when(itemRepository.findAllByOwnerId(OWNER_ID)).thenReturn(items);
        when(bookingRepository.findByItemIdsWithItem(List.of(1L, 2L))).thenReturn(bookings);
        when(commentRepository.findByItemIdsWithAuthorNameAndItemId(List.of(1L, 2L))).thenReturn(commentViews);

        List<ItemWithBookingDTO> result = itemService.getAllByOwner(OWNER_ID);

        assertThat(result).hasSize(2);
        ItemWithBookingDTO dto1 = result.stream().filter(d -> d.id().equals(1L)).findFirst().orElseThrow();
        assertThat(dto1.lastBooking()).isNotNull();
        assertThat(dto1.nextBooking()).isNotNull();
        assertThat(dto1.comments()).hasSize(1);

        ItemWithBookingDTO dto2 = result.stream().filter(d -> d.id().equals(2L)).findFirst().orElseThrow();
        assertThat(dto2.lastBooking()).isNull();
        assertThat(dto2.nextBooking()).isNull();
        assertThat(dto2.comments()).hasSize(1);
    }

    @Test
    void getAllByOwner_OwnerDoesNotExist_ShouldThrowNotFoundException() {
        doThrow(new NotFoundException("Пользователь с id = " + OWNER_ID + " не найден"))
                .when(userService).isExistsOrElseThrow(OWNER_ID);

        assertThatThrownBy(() -> itemService.getAllByOwner(OWNER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = " + OWNER_ID + " не найден");
    }

    @Test
    void getAllByOwner_NoItems_ShouldReturnEmptyList() {
        doNothing().when(userService).isExistsOrElseThrow(OWNER_ID);
        when(itemRepository.findAllByOwnerId(OWNER_ID)).thenReturn(Collections.emptyList());

        List<ItemWithBookingDTO> result = itemService.getAllByOwner(OWNER_ID);
        assertThat(result).isEmpty();
        verify(bookingRepository, never()).findByItemIdsWithItem(anyList());
        verify(commentRepository, never()).findByItemIdsWithAuthorNameAndItemId(anyList());
    }

    // --- Тесты для search() ---

    @Test
    void search_WithValidText_ShouldReturnMatchingItems() {
        String searchText = "test";
        Item item = new Item(1L, OWNER_ID, "Test Item", "Test Desc", true, 1L);
        List<Item> foundItems = List.of(item);
        List<ItemResponseDTO> expectedResponses = ItemMapper.mapToResponseDTOList(foundItems);

        when(itemRepository.findByText(searchText)).thenReturn(foundItems);

        Collection<ItemResponseDTO> result = itemService.search(searchText);

        assertThat(result).containsExactlyElementsOf(expectedResponses);
        verify(itemRepository).findByText(searchText);
    }

    @Test
    void search_WithEmptyText_ShouldReturnEmptyList() {
        Collection<ItemResponseDTO> result = itemService.search("");
        assertThat(result).isEmpty();
        verify(itemRepository, never()).findByText(anyString());
    }

    @Test
    void search_WithNullText_ShouldReturnEmptyList() {
        Collection<ItemResponseDTO> result = itemService.search(null);
        assertThat(result).isEmpty();
        verify(itemRepository, never()).findByText(anyString());
    }

    // --- Тесты для update() ---

    @Test
    void update_ExistingItemByOwner_ShouldUpdateItem() {
        Long itemId = 1L;
        ItemUpdateDTO updateDTO = new ItemUpdateDTO("Updated Name", "Updated Description", false, 2L);
        Item existingItem = new Item(itemId, OWNER_ID, "Old Name", "Old Description", true, 1L);
        Item updatedItem = new Item(itemId, OWNER_ID, "Updated Name", "Updated Description", false, 2L);
        ItemResponseDTO expectedResponse = ItemMapper.mapToResponseDTO(updatedItem);

        when(itemRepository.existsById(itemId)).thenReturn(true);
        doNothing().when(userService).isExistsOrElseThrow(OWNER_ID);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        ItemResponseDTO result = itemService.update(itemId, OWNER_ID, updateDTO);

        assertThat(result).isEqualTo(expectedResponse);
        verify(itemRepository).save(any(Item.class));
        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(captor.capture());
        Item savedItem = captor.getValue();
        assertThat(savedItem.getName()).isEqualTo("Updated Name");
        assertThat(savedItem.getDescription()).isEqualTo("Updated Description");
        assertThat(savedItem.getAvailable()).isFalse();
        assertThat(savedItem.getRequestId()).isEqualTo(2L);
    }

    @Test
    void update_NonExistingItem_ShouldThrowNotFoundException() {
        when(itemRepository.existsById(NON_EXISTING_ITEM_ID)).thenReturn(false);
        ItemUpdateDTO updateDTO = new ItemUpdateDTO("Name", "Desc", true, 1L);

        assertThatThrownBy(() -> itemService.update(NON_EXISTING_ITEM_ID, OWNER_ID, updateDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Вещь с ID = " + NON_EXISTING_ITEM_ID + " не найдена");
    }

    @Test
    void update_OwnerDoesNotExist_ShouldThrowNotFoundException() {
        when(itemRepository.existsById(EXISTING_ITEM_ID)).thenReturn(true);
        doThrow(new NotFoundException("Пользователь с id = " + OWNER_ID + " не найден"))
                .when(userService).isExistsOrElseThrow(OWNER_ID);
        ItemUpdateDTO updateDTO = new ItemUpdateDTO("Name", "Desc", true, 1L);

        assertThatThrownBy(() -> itemService.update(EXISTING_ITEM_ID, OWNER_ID, updateDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = " + OWNER_ID + " не найден");
    }

    @Test
    void update_AnotherOwnerTriesToUpdate_ShouldThrowPermissionException() {
        Long itemId = 1L;
        ItemUpdateDTO updateDTO = new ItemUpdateDTO("Updated Name", "Updated Description", false, 2L);
        Item existingItem = new Item(itemId, OWNER_ID, "Old Name", "Old Description", true, 1L);

        when(itemRepository.existsById(itemId)).thenReturn(true);
        doNothing().when(userService).isExistsOrElseThrow(ANOTHER_OWNER_ID);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));

        String expectedMessage = String.format(Validator.ITEM_PERMISSION_ERR_TEXT, ANOTHER_OWNER_ID, itemId);
        assertThatThrownBy(() -> itemService.update(itemId, ANOTHER_OWNER_ID, updateDTO))
                .isInstanceOf(PermissionException.class)
                .hasMessageContaining(expectedMessage);
    }

    // --- Тесты для delete() ---

    @Test
    void delete_ExistingItemByOwner_ShouldDeleteItem() {
        Long itemId = 1L;
        Item existingItem = new Item(itemId, OWNER_ID, "Item", "Desc", true, 1L);

        when(itemRepository.existsById(itemId)).thenReturn(true);
        doNothing().when(userService).isExistsOrElseThrow(OWNER_ID);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));

        itemService.delete(itemId, OWNER_ID);

        verify(itemRepository).deleteById(itemId);
    }

    @Test
    void delete_NonExistingItem_ShouldThrowNotFoundException() {
        when(itemRepository.existsById(NON_EXISTING_ITEM_ID)).thenReturn(false);

        assertThatThrownBy(() -> itemService.delete(NON_EXISTING_ITEM_ID, OWNER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Вещь с ID = " + NON_EXISTING_ITEM_ID + " не найдена");
    }

    @Test
    void delete_OwnerDoesNotExist_ShouldThrowNotFoundException() {
        when(itemRepository.existsById(EXISTING_ITEM_ID)).thenReturn(true);
        doThrow(new NotFoundException("Пользователь с id = " + OWNER_ID + " не найден"))
                .when(userService).isExistsOrElseThrow(OWNER_ID);

        assertThatThrownBy(() -> itemService.delete(EXISTING_ITEM_ID, OWNER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = " + OWNER_ID + " не найден");
    }

    @Test
    void delete_AnotherOwnerTriesToDelete_ShouldThrowPermissionException() {
        Long itemId = 1L;
        Item existingItem = new Item(itemId, OWNER_ID, "Item", "Desc", true, 1L);

        when(itemRepository.existsById(itemId)).thenReturn(true);
        doNothing().when(userService).isExistsOrElseThrow(ANOTHER_OWNER_ID);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));

        String expectedMessage = String.format(Validator.ITEM_PERMISSION_ERR_TEXT, ANOTHER_OWNER_ID, itemId);
        assertThatThrownBy(() -> itemService.delete(itemId, ANOTHER_OWNER_ID))
                .isInstanceOf(PermissionException.class)
                .hasMessageContaining(expectedMessage);
    }

    // --- Тесты для throwIfNotExists() ---

    @Test
    void throwIfNotExists_WhenItemExists_ShouldNotThrow() {
        when(itemRepository.existsById(EXISTING_ITEM_ID)).thenReturn(true);
        itemService.isExistsOrElseThrow(EXISTING_ITEM_ID);
        // no exception
    }

    @Test
    void throwIfNotExists_WhenItemDoesNotExist_ShouldThrowNotFoundException() {
        when(itemRepository.existsById(NON_EXISTING_ITEM_ID)).thenReturn(false);
        assertThatThrownBy(() -> itemService.isExistsOrElseThrow(NON_EXISTING_ITEM_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Вещь с ID = " + NON_EXISTING_ITEM_ID + " не найдена");
    }

    // --- Тесты для createComment() ---

    @Test
    void createComment_ValidData_ShouldCreateComment() {
        Long itemId = 1L;
        Long userId = 100L;
        String text = "Great item!";
        Item item = new Item(itemId, OWNER_ID, "Item", "Desc", true, null);
        User user = new User(userId, "User", "user@mail.com");
        Comment savedComment = new Comment(10L, text, item, user, now);
        CommentResponseDTO expectedResponse = CommentMapper.mapToResponseDTO(savedComment);

        when(itemRepository.existsById(itemId)).thenReturn(true);
        doNothing().when(userService).isExistsOrElseThrow(userId);
        when(bookingRepository.findPastApprovedBookingsByItemIdAndBookerId(eq(itemId), eq(userId), any(LocalDateTime.class)))
                .thenReturn(List.of(new Booking()));
        when(itemRepository.getReferenceById(itemId)).thenReturn(item);
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentResponseDTO result = itemService.createComment(itemId, userId, text);

        assertThat(result).isEqualTo(expectedResponse);
        verify(commentRepository).save(any(Comment.class));
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        Comment captured = captor.getValue();
        assertThat(captured.getText()).isEqualTo(text);
        assertThat(captured.getItem()).isEqualTo(item);
        assertThat(captured.getAuthor()).isEqualTo(user);
    }

    @Test
    void createComment_ItemNotFound_ShouldThrowNotFoundException() {
        when(itemRepository.existsById(NON_EXISTING_ITEM_ID)).thenReturn(false);

        assertThatThrownBy(() -> itemService.createComment(NON_EXISTING_ITEM_ID, USER_ID, "text"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Вещь с ID = " + NON_EXISTING_ITEM_ID + " не найдена");
    }

    @Test
    void createComment_UserNotFound_ShouldThrowNotFoundException() {
        when(itemRepository.existsById(EXISTING_ITEM_ID)).thenReturn(true);
        doThrow(new NotFoundException("Пользователь с id = " + USER_ID + " не найден"))
                .when(userService).isExistsOrElseThrow(USER_ID);

        assertThatThrownBy(() -> itemService.createComment(EXISTING_ITEM_ID, USER_ID, "text"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = " + USER_ID + " не найден");
    }

    @Test
    void createComment_UserNeverBooked_ShouldThrowPermissionException() {
        Long itemId = 1L;
        Long userId = 100L;
        when(itemRepository.existsById(itemId)).thenReturn(true);
        doNothing().when(userService).isExistsOrElseThrow(userId);
        when(bookingRepository.findPastApprovedBookingsByItemIdAndBookerId(eq(itemId), eq(userId), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> itemService.createComment(itemId, userId, "text"))
                .isInstanceOf(LogicException.class)
                .hasMessageContaining(Validator.COMMENT_CREATE_LOGIC_ERR_TEXT);
    }

    // --- Вспомогательные методы ---
    private Booking createBooking(Long id, LocalDateTime start, LocalDateTime end, BookingStatus status, Item item) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(status);
        booking.setItem(item);
        return booking;
    }
}