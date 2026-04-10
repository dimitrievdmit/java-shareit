package ru.practicum.shareit.request.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(name = "requestor_id", nullable = false)
    private Long requestorId;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime created;
}