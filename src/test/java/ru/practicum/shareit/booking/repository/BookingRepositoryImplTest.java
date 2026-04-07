package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookingRepositoryImplTest extends BaseBookingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    private Item testItem;
    private User testBooker;
    private User otherBooker;
    private Long ownerId;

    @BeforeEach
    @Transactional
    void setUp() {
        super.bookingRepository = this.bookingRepository;
        entityManager.clear();

        User owner = new User(null, "Owner", "owner@mail.com");
        testBooker = new User(null, "Booker", "booker@mail.com");
        otherBooker = new User(null, "Other", "other@mail.com");
        entityManager.persist(owner);
        entityManager.persist(testBooker);
        entityManager.persist(otherBooker);

        testItem = new Item(null, owner.getId(), "TestItem", "Desc", true, null);
        entityManager.persist(testItem);

        ownerId = owner.getId();

        entityManager.flush();
        entityManager.clear();
    }

    @AfterEach
    @Transactional
    void tearDown() {
        bookingRepository.deleteAll();
        entityManager.clear();
    }

    @Override
    protected Item getTestItem() {
        return entityManager.find(Item.class, testItem.getId());
    }

    @Override
    protected User getTestBooker() {
        return entityManager.find(User.class, testBooker.getId());
    }

    @Override
    protected User getOtherBooker() {
        return entityManager.find(User.class, otherBooker.getId());
    }

    @Override
    protected Long getOwnerId() {
        return ownerId;
    }

    @Override
    protected Item createAndSaveItem(String name, String description) {
        Item item = new Item(null, ownerId, name, description, true, null);
        entityManager.persist(item);
        entityManager.flush();
        return item;
    }
}