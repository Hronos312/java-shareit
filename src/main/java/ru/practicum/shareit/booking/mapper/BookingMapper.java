package ru.practicum.shareit.booking.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;

@Component
public class BookingMapper {

    public BookingDto toBookingDto(Booking booking) {
        BookingDto bookingDto = new BookingDto();

        bookingDto.setId(booking.getId());
        bookingDto.setStart(booking.getStart());
        bookingDto.setEnd(booking.getEnd());
        bookingDto.setItem(booking.getItem());
        bookingDto.setBooker(booking.getBooker());
        bookingDto.setStatus(booking.getStatus());

        return bookingDto;
    }

    public BookingShortDto toBookingShortDto(Booking booking) {
        BookingShortDto dto = new BookingShortDto();

        dto.setId(booking.getId());
        dto.setBookerId(booking.getBooker().getId());

        return dto;
    }
}