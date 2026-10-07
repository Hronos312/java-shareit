package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.util.Collection;

public interface BookingService {

    Booking create(Long userId, BookingRequestDto bookingRequestDto);

    Booking approve(Long userId, Long bookingId, Boolean approved);

    Booking findById(Long userId, Long bookingId);

    Collection<Booking> findAllByBooker(Long userId, BookingState state, Integer from, Integer size);

    Collection<Booking> findAllByOwner(Long userId, BookingState state, Integer from, Integer size);
}