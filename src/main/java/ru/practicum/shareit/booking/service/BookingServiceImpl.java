package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public Booking create(Long userId, BookingRequestDto bookingRequestDto) {

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id " + bookingRequestDto.getItemId() + " не найдена"));

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец вещи не может её бронировать");
        }

        LocalDateTime now = LocalDateTime.now();

        if (bookingRequestDto.getStart() == null || bookingRequestDto.getEnd() == null) {
            throw new ValidationException("Необходимо указать даты бронирования");
        }

        if (!bookingRequestDto.getStart().isAfter(now)) {
            throw new ValidationException("Дата начала бронирования должна быть в будущем");
        }

        if (!bookingRequestDto.getEnd().isAfter(bookingRequestDto.getStart())) {
            throw new ValidationException("Дата окончания должна быть позже даты начала");
        }

        Booking booking = new Booking();

        booking.setStart(bookingRequestDto.getStart());
        booking.setEnd(bookingRequestDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        return bookingRepository.save(booking);
    }

    @Override
    public Booking approve(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + " не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Подтвердить бронирование может только владелец вещи");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return bookingRepository.save(booking);
    }

    @Override
    public Booking findById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + " не найдено"));

        Long bookerId = booking.getBooker().getId();
        Long ownerId = booking.getItem().getOwner().getId();

        if (!bookerId.equals(userId) && !ownerId.equals(userId)) {
            throw new NotFoundException("Пользователь не имеет доступа к этому бронированию");
        }

        return booking;
    }

    @Override
    public Collection<Booking> findAllByBooker(Long userId, String state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        BookingState bookingState;

        try {
            bookingState = BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Неизвестное состояние бронирования: " + state);
        }

        LocalDateTime now = LocalDateTime.now();

        return switch (bookingState) {
            case ALL -> bookingRepository.findAllByBookerIdOrderByStartDesc(userId);

            case CURRENT ->
                    bookingRepository
                            .findAllByBookerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(userId, now, now);

            case PAST -> bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, now);

            case FUTURE -> bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, now);

            case WAITING -> bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);

            case REJECTED -> bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
        };
    }

    @Override
    public Collection<Booking> findAllByOwner(Long userId, String state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        BookingState bookingState;

        try {
            bookingState = BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Неизвестное состояние бронирования: " + state);
        }

        LocalDateTime now = LocalDateTime.now();

        return switch (bookingState) {
            case ALL -> bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId);

            case CURRENT ->
                    bookingRepository
                            .findAllByItemOwnerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(userId, now, now);

            case PAST -> bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now);

            case FUTURE -> bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now);

            case WAITING -> bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);

            case REJECTED -> bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
        };
    }
}
