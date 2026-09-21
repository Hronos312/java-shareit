package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
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
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
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
    @Transactional
    public Booking approve(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + " не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Подтвердить бронирование может только владелец вещи");
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
            throw new ForbiddenException("Пользователь не имеет доступа к этому бронированию");
        }

        return booking;
    }

    @Override
    public Collection<Booking> findAllByBooker(Long userId, BookingState state) {
        checkUserExists(userId);

        return findBookings(userId, state, false);
    }

    @Override
    public Collection<Booking> findAllByOwner(Long userId, BookingState state) {
        checkUserExists(userId);

        return findBookings(userId, state, true);
    }

    private void checkUserExists(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    private Collection<Booking> findBookings(Long userId, BookingState state, boolean owner) {
        LocalDateTime now = LocalDateTime.now();
        Sort sort = Sort.by(Sort.Direction.DESC, "start");

        return switch (state) {
            case ALL -> bookingRepository.findAllForUser(userId, owner, sort);

            case CURRENT -> bookingRepository.findCurrentForUser(userId, owner, now, sort);

            case PAST -> bookingRepository.findPastForUser(userId, owner, now, sort);

            case FUTURE -> bookingRepository.findFutureForUser(userId, owner, now, sort);

            case WAITING -> bookingRepository.findByStatusForUser(userId, owner, BookingStatus.WAITING, sort);

            case REJECTED -> bookingRepository.findByStatusForUser(userId, owner, BookingStatus.REJECTED, sort);
        };
    }
}
