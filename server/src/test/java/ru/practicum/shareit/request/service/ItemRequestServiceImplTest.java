package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private final Long userId = 1L;
    private final Long requestId = 10L;
    private ItemRequestCreateDto createDto;
    private ItemRequest request;
    private ItemRequestResponseDto responseDto;

    @BeforeEach
    void setUp() {
        createDto = new ItemRequestCreateDto("Нужна вещь");
        request = new ItemRequest();
        request.setId(requestId);
        request.setDescription("Нужна вещь");
        request.setRequestorId(userId);
        request.setCreated(LocalDateTime.now());
        responseDto = ItemRequestMapper.mapToResponseDTO(request, List.of());
    }

    // --- create ---
    @Test
    void create_ValidData_ShouldCreateRequest() {
        doNothing().when(userService).isExistsOrElseThrow(userId);
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(request);

        ItemRequestResponseDto result = itemRequestService.create(createDto, userId);

        assertThat(result.id()).isEqualTo(requestId);
        assertThat(result.description()).isEqualTo("Нужна вещь");
        assertThat(result.requestorId()).isEqualTo(userId);
        verify(userService).isExistsOrElseThrow(userId);
        verify(itemRequestRepository).save(any(ItemRequest.class));
    }

    @Test
    void create_UserNotFound_ShouldThrowNotFoundException() {
        doThrow(new NotFoundException("User not found")).when(userService).isExistsOrElseThrow(userId);

        assertThatThrownBy(() -> itemRequestService.create(createDto, userId))
                .isInstanceOf(NotFoundException.class);
        verify(itemRequestRepository, never()).save(any());
    }

    // --- getUserRequests ---
    @Test
    void getUserRequests_ShouldReturnSortedRequestsWithItems() {
        doNothing().when(userService).isExistsOrElseThrow(userId);
        Sort sort = Sort.by("created").descending();
        when(itemRequestRepository.findByRequestorId(userId, sort)).thenReturn(List.of(request));

        Item item = new Item(100L, 2L, "Ответная вещь", "desc", true, requestId);
        when(itemRepository.findAllByRequestIdIn(List.of(requestId))).thenReturn(List.of(item));

        List<ItemRequestResponseDto> result = (List<ItemRequestResponseDto>) itemRequestService.getUserRequests(userId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().items()).hasSize(1);
        assertThat(result.getFirst().items().getFirst().id()).isEqualTo(100L);
        verify(itemRequestRepository).findByRequestorId(userId, sort);
    }

    // --- getNonUserRequests ---
    @Test
    void getNonUserRequests_ShouldReturnPagedRequests() {
        doNothing().when(userService).isExistsOrElseThrow(userId);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("created").descending());
        when(itemRequestRepository.findAllByRequestorIdNot(eq(userId), any(Pageable.class)))
                .thenReturn(List.of(request));

        List<ItemRequestResponseDto> result = (List<ItemRequestResponseDto>)
                itemRequestService.getNonUserRequests(0, 10, userId);

        assertThat(result).hasSize(1);
        verify(itemRequestRepository).findAllByRequestorIdNot(eq(userId), any(Pageable.class));
    }

    // --- getById ---
    @Test
    void getById_ValidData_ShouldReturnRequestWithItems() {
        doNothing().when(userService).isExistsOrElseThrow(userId);
        when(itemRequestRepository.existsById(requestId)).thenReturn(true);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        Item item = new Item(200L, 3L, "Вещь в ответ", "desc", true, requestId);
        when(itemRepository.findAllByRequestId(requestId)).thenReturn(List.of(item));

        ItemRequestResponseDto result = itemRequestService.getById(requestId, userId);

        assertThat(result.id()).isEqualTo(requestId);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().id()).isEqualTo(200L);
    }

    @Test
    void getById_RequestNotFound_ShouldThrowNotFoundException() {
        doNothing().when(userService).isExistsOrElseThrow(userId);
        when(itemRequestRepository.existsById(requestId)).thenReturn(false);

        assertThatThrownBy(() -> itemRequestService.getById(requestId, userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Запрос вещи с id = " + requestId + " не найден");
    }

    // --- isExistsOrElseThrow ---
    @Test
    void isExistsOrElseThrow_WhenExists_ShouldNotThrow() {
        when(itemRequestRepository.existsById(requestId)).thenReturn(true);
        itemRequestService.isExistsOrElseThrow(requestId);
        // no exception
    }

    @Test
    void isExistsOrElseThrow_WhenNotExists_ShouldThrowNotFoundException() {
        when(itemRequestRepository.existsById(requestId)).thenReturn(false);
        assertThatThrownBy(() -> itemRequestService.isExistsOrElseThrow(requestId))
                .isInstanceOf(NotFoundException.class);
    }
}