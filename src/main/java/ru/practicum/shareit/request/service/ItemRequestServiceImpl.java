package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.item.ItemShortDTO;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.shareit.request.mapper.ItemRequestMapper.mapToResponseDTO;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    @Transactional
    public ItemRequestResponseDto create(ItemRequestCreateDto dto, Long userId) {
        log.info("Создание запроса вещи пользователем {}", userId);
        userService.throwIfNotExists(userId);

        ItemRequest request = ItemRequestMapper.mapToDomain(dto, userId, LocalDateTime.now());
        ItemRequest saved = itemRequestRepository.save(request);
        return mapToResponseDTO(saved, List.of());
    }

    @Override
    public Collection<ItemRequestResponseDto> getUserRequests(Long userId) {
        log.info("Получение запросов пользователя {}", userId);
        userService.throwIfNotExists(userId);

        Sort sort = Sort.by("created").descending();
        List<ItemRequest> requests = itemRequestRepository.findByRequestorId(userId, sort);

        return enrichWithItems(requests);
    }

    @Override
    public Collection<ItemRequestResponseDto> getNonUserRequests(Integer from, Integer size, Long userId) {
        log.info("Получение запросов других пользователей (from={}, size={}) для userId={}", from, size, userId);
        userService.throwIfNotExists(userId);

        Pageable pageable = PageRequest.of(from, size, Sort.by("created").descending());

        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdNot(userId, pageable);

        return enrichWithItems(requests);
    }

    @Override
    public ItemRequestResponseDto getById(Long requestId, Long userId) {
        log.info("Получение запроса {} пользователем {}", requestId, userId);
        userService.throwIfNotExists(userId);
        isExistsOrElseThrow(requestId);

        ItemRequest request = itemRequestRepository.findById(requestId).orElseThrow();
        List<ItemShortDTO> items = findItemsByRequestId(requestId);
        return mapToResponseDTO(request, items);
    }

    @Override
    public void isExistsOrElseThrow(Long id) {
        if (!itemRequestRepository.existsById(id)) {
            String errText = "Запрос вещи с id = " + id + " не найден";
            log.error(errText);
            throw new NotFoundException(errText);
        }
    }

    // Вспомогательный метод для обогащения списка запросов их ответами (вещами)
    private Collection<ItemRequestResponseDto> enrichWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }

        // Получаем все requestId из списка
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        // Загружаем все вещи, привязанные к этим запросам, одним запросом
        List<Item> allItems = itemRepository.findAllByRequestIdIn(requestIds);
        // Группируем вещи по requestId
        Map<Long, List<ItemShortDTO>> itemsByRequestId = allItems.stream()
                .collect(Collectors.groupingBy(
                        Item::getRequestId,
                        Collectors.mapping(ItemMapper::mapToShortDTO, Collectors.toList())
                ));

        // Формируем DTO для каждого запроса
        return requests.stream()
                .map(req -> mapToResponseDTO(req, itemsByRequestId.getOrDefault(req.getId(), List.of())))
                .collect(Collectors.toList());
    }

    // Вспомогательный метод для получения ответов по одному requestId
    private List<ItemShortDTO> findItemsByRequestId(Long requestId) {
        Collection<Item> items = itemRepository.findAllByRequestId(requestId);
        return ItemMapper.mapToShortDTOList(items);
    }
}