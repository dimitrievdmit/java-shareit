package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
class ItemRequestRepositoryTest {

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private final Long requestor1 = 1L;
    private final Long requestor2 = 2L;
    private final Long requestor3 = 3L;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        itemRequestRepository.deleteAll();
    }

    private ItemRequest createRequest(Long requestorId, LocalDateTime created) {
        ItemRequest request = new ItemRequest();
        request.setDescription("Нужна вещь от " + requestorId);
        request.setRequestorId(requestorId);
        request.setCreated(created);
        return request;
    }

    @Test
    void save_ShouldGenerateId() {
        ItemRequest request = createRequest(requestor1, now);
        ItemRequest saved = itemRequestRepository.save(request);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void findByRequestorId_ShouldReturnSortedDesc() {
        LocalDateTime time1 = now.minusDays(2);
        LocalDateTime time2 = now.minusDays(1);
        LocalDateTime time3 = now;

        itemRequestRepository.save(createRequest(requestor1, time1));
        itemRequestRepository.save(createRequest(requestor1, time2));
        itemRequestRepository.save(createRequest(requestor1, time3));

        Sort sort = Sort.by("created").descending();
        List<ItemRequest> requests = itemRequestRepository.findByRequestorId(requestor1, sort);

        assertThat(requests).hasSize(3);
        assertThat(requests.get(0).getCreated()).isEqualTo(time3);
        assertThat(requests.get(1).getCreated()).isEqualTo(time2);
        assertThat(requests.get(2).getCreated()).isEqualTo(time1);
    }

    @Test
    void findAllByRequestorIdNot_ShouldExcludeUserAndUsePagination() {
        itemRequestRepository.save(createRequest(requestor1, now));
        itemRequestRepository.save(createRequest(requestor2, now));
        itemRequestRepository.save(createRequest(requestor3, now));

        Pageable pageable = PageRequest.of(0, 2, Sort.by("created").descending());
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdNot(requestor1, pageable);

        assertThat(requests).hasSize(2);
        assertThat(requests).extracting(ItemRequest::getRequestorId)
                .doesNotContain(requestor1);
    }
}