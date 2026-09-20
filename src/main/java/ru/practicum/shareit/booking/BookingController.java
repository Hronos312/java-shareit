package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.Collection;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;

    @PostMapping
    public BookingDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                             @RequestBody BookingRequestDto bookingRequestDto) {

        Booking booking = bookingService.create(userId, bookingRequestDto);

        return bookingMapper.toBookingDto(booking);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable Long bookingId,
                              @RequestParam Boolean approved) {

        Booking booking = bookingService.approve(userId, bookingId, approved);

        return bookingMapper.toBookingDto(booking);
    }

    @GetMapping("/{bookingId}")
    public BookingDto findById(@RequestHeader("X-Sharer-User-Id") Long userId,
                               @PathVariable Long bookingId) {

        Booking booking = bookingService.findById(userId, bookingId);

        return bookingMapper.toBookingDto(booking);
    }

    @GetMapping
    public Collection<BookingDto> findAllByBooker(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @RequestParam(defaultValue = "ALL") String state) {

        return bookingService.findAllByBooker(userId, state)
                .stream()
                .map(bookingMapper::toBookingDto)
                .toList();
    }

    @GetMapping("/owner")
    public Collection<BookingDto> findAllByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @RequestParam(defaultValue = "ALL") String state) {

        return bookingService.findAllByOwner(userId, state)
                .stream()
                .map(bookingMapper::toBookingDto)
                .toList();
    }
}