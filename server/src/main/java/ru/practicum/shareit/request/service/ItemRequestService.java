package ru.practicum.shareit.request.service;


import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.Collection;

public interface ItemRequestService {

    // ИД пользователя должен существовать, иначе выбросить 404.
    ItemRequestResponseDto create(ItemRequestCreateDto itemRequestCreateDto, Long userId);

    // ИД пользователя должен существовать, иначе выбросить 404.
    // Запросы должны возвращаться отсортированными от более новых к более старым.
    Collection<ItemRequestResponseDto> getUserRequests(Long userId);

    // ИД пользователя должен существовать, иначе выбросить 404.
    // Запросы должны возвращаться отсортированными от более новых к более старым.
    Collection<ItemRequestResponseDto> getNonUserRequests(Integer from, Integer size, Long userId);

    // Запрос должен существовать. Иначе, ошибка 404.
    // ИД пользователя должен существовать, иначе выбросить 404.
    ItemRequestResponseDto getById(Long requestId, Long userId);

    // Проверка, что запрос существует.
    void isExistsOrElseThrow(Long id);
}
