package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.util.Collection;

public interface BookingService {

    Booking create(Long userId, BookingRequestDto bookingRequestDto);

    Booking approve(Long userId, Long bookingId, Boolean approved);

    Booking findById(Long userId, Long bookingId);

    Collection<Booking> findAllByBooker(Long userId, String state);

    Collection<Booking> findAllByOwner(Long userId, String state);
}