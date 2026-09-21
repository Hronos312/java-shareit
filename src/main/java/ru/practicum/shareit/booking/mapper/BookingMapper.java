package ru.practicum.shareit.booking.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.dto.BookingUserDto;

@Component
public class BookingMapper {

    public BookingDto toBookingDto(Booking booking) {
        BookingDto dto = new BookingDto();

        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setItem(toItemDto(booking));
        dto.setBooker(toUserDto(booking));
        dto.setStatus(booking.getStatus());

        return dto;
    }

    public BookingShortDto toBookingShortDto(Booking booking) {
        BookingShortDto dto = new BookingShortDto();

        dto.setId(booking.getId());
        dto.setBookerId(booking.getBooker().getId());

        return dto;
    }

    private BookingItemDto toItemDto(Booking booking) {
        BookingItemDto dto = new BookingItemDto();

        dto.setId(booking.getItem().getId());
        dto.setName(booking.getItem().getName());

        return dto;
    }

    private BookingUserDto toUserDto(Booking booking) {
        BookingUserDto dto = new BookingUserDto();

        dto.setId(booking.getBooker().getId());
        dto.setName(booking.getBooker().getName());

        return dto;
    }

}