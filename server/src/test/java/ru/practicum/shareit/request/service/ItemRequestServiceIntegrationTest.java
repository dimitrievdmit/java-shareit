package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@Transactional
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @MockBean
    private UserService userService;

    @MockBean
    private ItemRepository itemRepository; // чтобы не загружать реальные вещи

    @Test
    void createAndGetUserRequests_Integration() {
        Long userId = 1L;
        doNothing().when(userService).isExistsOrElseThrow(userId);

        ItemRequestCreateDto createDto = new ItemRequestCreateDto("Интеграционный запрос");
        ItemRequestResponseDto created = itemRequestService.create(createDto, userId);

        assertThat(created.id()).isNotNull();
        assertThat(created.description()).isEqualTo("Интеграционный запрос");

        var userRequests = itemRequestService.getUserRequests(userId);
        assertThat(userRequests).hasSize(1);
        assertThat(userRequests.iterator().next().id()).isEqualTo(created.id());
    }
}