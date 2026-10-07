package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void createShouldRejectUnavailableItem() {
        User booker = user(2L);
        User owner = user(1L);

        Item item = item(5L, owner, false);

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));

        when(itemRepository.findById(5L))
                .thenReturn(Optional.of(item));

        assertThatThrownBy(
                () -> bookingService.create(
                        2L,
                        request(
                                5L,
                                LocalDateTime.now().plusHours(1),
                                LocalDateTime.now().plusHours(2)
                        )
                )
        )
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createShouldRejectOwnerBookingOwnItem() {
        User owner = user(1L);
        Item item = item(5L, owner, true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));

        when(itemRepository.findById(5L))
                .thenReturn(Optional.of(item));

        assertThatThrownBy(
                () -> bookingService.create(
                        1L,
                        request(
                                5L,
                                LocalDateTime.now().plusHours(1),
                                LocalDateTime.now().plusHours(2)
                        )
                )
        )
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createShouldRejectMissingDates() {
        User booker = user(2L);
        User owner = user(1L);
        Item item = item(5L, owner, true);

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));

        when(itemRepository.findById(5L))
                .thenReturn(Optional.of(item));

        assertThatThrownBy(
                () -> bookingService.create(
                        2L,
                        request(5L, null, null)
                )
        )
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createShouldRejectMissingEndDate() {
        User booker = user(2L);
        User owner = user(1L);
        Item item = item(5L, owner, true);

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));

        when(itemRepository.findById(5L))
                .thenReturn(Optional.of(item));

        assertThatThrownBy(
                () -> bookingService.create(
                        2L,
                        request(
                                5L,
                                LocalDateTime.now().plusHours(1),
                                null
                        )
                )
        )
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createShouldRejectPastStart() {
        User booker = user(2L);
        User owner = user(1L);
        Item item = item(5L, owner, true);

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));

        when(itemRepository.findById(5L))
                .thenReturn(Optional.of(item));

        assertThatThrownBy(
                () -> bookingService.create(
                        2L,
                        request(
                                5L,
                                LocalDateTime.now().minusHours(1),
                                LocalDateTime.now().plusHours(1)
                        )
                )
        )
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createShouldRejectEndBeforeStart() {
        User booker = user(2L);
        User owner = user(1L);
        Item item = item(5L, owner, true);

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));

        when(itemRepository.findById(5L))
                .thenReturn(Optional.of(item));

        LocalDateTime start =
                LocalDateTime.now().plusHours(2);

        assertThatThrownBy(
                () -> bookingService.create(
                        2L,
                        request(
                                5L,
                                start,
                                start.minusHours(1)
                        )
                )
        )
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void approveShouldRejectWrongOwner() {
        User owner = user(1L);
        Item item = item(5L, owner, true);

        Booking booking =
                booking(10L, item, user(2L));

        when(bookingRepository.findById(10L))
                .thenReturn(Optional.of(booking));

        assertThatThrownBy(
                () -> bookingService.approve(
                        3L,
                        10L,
                        true
                )
        )
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void approveShouldRejectAlreadyProcessedBooking() {
        User owner = user(1L);
        Item item = item(5L, owner, true);

        Booking booking =
                booking(10L, item, user(2L));

        booking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(10L))
                .thenReturn(Optional.of(booking));

        assertThatThrownBy(
                () -> bookingService.approve(
                        1L,
                        10L,
                        true
                )
        )
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void approveFalseShouldRejectBooking() {
        User owner = user(1L);
        Item item = item(5L, owner, true);

        Booking booking =
                booking(10L, item, user(2L));

        when(bookingRepository.findById(10L))
                .thenReturn(Optional.of(booking));

        when(bookingRepository.save(booking))
                .thenReturn(booking);

        Booking result =
                bookingService.approve(
                        1L,
                        10L,
                        false
                );

        assertThat(result.getStatus())
                .isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void findByIdShouldAllowBooker() {
        User owner = user(1L);
        User booker = user(2L);
        Item item = item(5L, owner, true);

        Booking booking =
                booking(10L, item, booker);

        when(bookingRepository.findById(10L))
                .thenReturn(Optional.of(booking));

        assertThat(
                bookingService.findById(2L, 10L)
        )
                .isEqualTo(booking);
    }

    @Test
    void findByIdShouldAllowOwner() {
        User owner = user(1L);
        User booker = user(2L);

        Booking booking =
                booking(
                        10L,
                        item(5L, owner, true),
                        booker
                );

        when(bookingRepository.findById(10L))
                .thenReturn(Optional.of(booking));

        assertThat(
                bookingService.findById(1L, 10L)
        )
                .isEqualTo(booking);
    }

    @Test
    void findByIdShouldRejectOtherUser() {
        Booking booking =
                booking(
                        10L,
                        item(
                                5L,
                                user(1L),
                                true
                        ),
                        user(2L)
                );

        when(bookingRepository.findById(10L))
                .thenReturn(Optional.of(booking));

        assertThatThrownBy(
                () -> bookingService.findById(
                        3L,
                        10L
                )
        )
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void findAllShouldCoverEveryState() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user(1L)));

        bookingService.findAllByBooker(
                1L, BookingState.ALL, 0, 10
        );

        bookingService.findAllByBooker(
                1L, BookingState.CURRENT, 0, 10
        );

        bookingService.findAllByBooker(
                1L, BookingState.PAST, 0, 10
        );

        bookingService.findAllByBooker(
                1L, BookingState.FUTURE, 0, 10
        );

        bookingService.findAllByBooker(
                1L, BookingState.WAITING, 0, 10
        );

        bookingService.findAllByBooker(
                1L, BookingState.REJECTED, 0, 10
        );

        bookingService.findAllByOwner(
                1L, BookingState.ALL, 0, 10
        );

        verify(bookingRepository)
                .findAllForUser(
                        eq(1L),
                        eq(false),
                        any(Pageable.class)
                );

        verify(bookingRepository)
                .findCurrentForUser(
                        eq(1L),
                        eq(false),
                        any(LocalDateTime.class),
                        any(Pageable.class)
                );

        verify(bookingRepository)
                .findPastForUser(
                        eq(1L),
                        eq(false),
                        any(LocalDateTime.class),
                        any(Pageable.class)
                );

        verify(bookingRepository)
                .findFutureForUser(
                        eq(1L),
                        eq(false),
                        any(LocalDateTime.class),
                        any(Pageable.class)
                );

        verify(bookingRepository, times(2))
                .findByStatusForUser(
                        eq(1L),
                        eq(false),
                        any(BookingStatus.class),
                        any(Pageable.class)
                );

        verify(bookingRepository)
                .findAllForUser(
                        eq(1L),
                        eq(true),
                        any(Pageable.class)
                );
    }

    @Test
    void findAllShouldRejectUnknownUser() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> bookingService.findAllByBooker(
                        999L,
                        BookingState.ALL,
                        0,
                        10
                )
        )
                .isInstanceOf(NotFoundException.class);

        verifyNoInteractions(bookingRepository);
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private Item item(
            Long id,
            User owner,
            boolean available) {

        Item item = new Item();
        item.setId(id);
        item.setOwner(owner);
        item.setAvailable(available);

        return item;
    }

    private Booking booking(
            Long id,
            Item item,
            User booker) {

        Booking booking = new Booking();
        booking.setId(id);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        return booking;
    }

    private BookingRequestDto request(
            Long itemId,
            LocalDateTime start,
            LocalDateTime end) {

        BookingRequestDto request =
                new BookingRequestDto();

        request.setItemId(itemId);
        request.setStart(start);
        request.setEnd(end);

        return request;
    }
}