package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Collection<Item> findAllByOwnerId(Long ownerId);

    Collection<Item> findAllByRequestId(Long requestId);

    List<Item> findAllByRequestIdIn(List<Long> requestIds);

    @Query("SELECT i FROM Item i WHERE " +
            "(LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "AND i.available = true " +
            "ORDER BY i.id ASC")
    Collection<Item> findByText(String text);
}
