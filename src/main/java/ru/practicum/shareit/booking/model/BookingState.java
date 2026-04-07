package ru.practicum.shareit.booking.model;

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static final String DEFAULT_VALUE = "ALL";
}
