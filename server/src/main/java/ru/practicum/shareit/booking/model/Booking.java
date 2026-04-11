package ru.practicum.shareit.booking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime start;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime end;

    // По тестам в ТЗ не существует сценария, когда не нужно выгружать item.id и item.name.
    // В связи с этим, несмотря на рекомендации в уроках, наиболее логичный тип выгрузки здесь EAGER.
    // Иначе, придётся переделать все методы Get на Query с Join Fetch,
    // включая переопределение наследуемых стандартных JPA методов,
    // а также, отказаться от функционала запросных методов.
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    // По тестам в ТЗ не существует сценария, когда не нужно выгружать booker.id
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "booker_id", nullable = false)
    private User booker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;
}