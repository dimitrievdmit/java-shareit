package ru.practicum.shareit.common.controller;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.user.client.UserClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class GatewayIntegrationTestBase {
    @MockBean
    protected BookingClient bookingClient;
    @MockBean
    protected ItemClient itemClient;
    @MockBean
    protected UserClient userClient;
    @MockBean
    protected ItemRequestClient itemRequestClient;
}