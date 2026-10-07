package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void createShouldPersistWaitingBooking() {
        User owner = createUser(
                "Owner",
                "owner-create@mail.ru"
        );

        User booker = createUser(
                "Booker",
                "booker-create@mail.ru"
        );

        Item item = createItem(owner, "Дрель");

        BookingRequestDto request =
                new BookingRequestDto();

        request.setItemId(item.getId());
        request.setStart(
                LocalDateTime.now().plusHours(1)
        );
        request.setEnd(
                LocalDateTime.now().plusHours(2)
        );

        Booking created =
                bookingService.create(
                        booker.getId(),
                        request
                );

        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus())
                .isEqualTo(BookingStatus.WAITING);

        assertThat(created.getBooker().getId())
                .isEqualTo(booker.getId());

        assertThat(created.getItem().getId())
                .isEqualTo(item.getId());

        Booking saved = bookingRepository
                .findById(created.getId())
                .orElseThrow();

        assertThat(saved.getStatus())
                .isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void approveShouldPersistApprovedStatus() {
        User owner = createUser(
                "Owner",
                "owner-approve@mail.ru"
        );

        User booker = createUser(
                "Booker",
                "booker-approve@mail.ru"
        );

        Item item = createItem(owner, "Перфоратор");

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(
                LocalDateTime.now().plusHours(1)
        );
        booking.setEnd(
                LocalDateTime.now().plusHours(2)
        );
        booking.setStatus(
                BookingStatus.WAITING
        );

        booking = bookingRepository.save(booking);

        Booking approved =
                bookingService.approve(
                        owner.getId(),
                        booking.getId(),
                        true
                );

        assertThat(approved.getStatus())
                .isEqualTo(BookingStatus.APPROVED);

        Booking saved = bookingRepository
                .findById(booking.getId())
                .orElseThrow();

        assertThat(saved.getStatus())
                .isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void findAllByBookerShouldFilterFutureBookingsAndApplyPagination() {
        User owner = createUser(
                "Owner",
                "owner-list@mail.ru"
        );

        User booker = createUser(
                "Booker",
                "booker-list@mail.ru"
        );

        Item item = createItem(owner, "Пила");

        createBooking(
                booker,
                item,
                LocalDateTime.now().plusHours(1)
        );

        createBooking(
                booker,
                item,
                LocalDateTime.now().plusHours(2)
        );

        createBooking(
                booker,
                item,
                LocalDateTime.now().plusHours(3)
        );

        Collection<Booking> result =
                bookingService.findAllByBooker(
                        booker.getId(),
                        BookingState.FUTURE,
                        1,
                        1
                );

        assertThat(result).hasSize(1);

        Booking booking =
                result.iterator().next();

        assertThat(booking.getStart())
                .isAfter(LocalDateTime.now());
    }

    private User createUser(
            String name,
            String email) {

        User user = new User();
        user.setName(name);
        user.setEmail(email);

        return userRepository.save(user);
    }

    private Item createItem(
            User owner,
            String name) {

        Item item = new Item();
        item.setName(name);
        item.setDescription("Описание");
        item.setAvailable(true);
        item.setOwner(owner);

        return itemRepository.save(item);
    }

    private Booking createBooking(
            User booker,
            Item item,
            LocalDateTime start) {

        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(start);
        booking.setEnd(
                start.plusHours(1)
        );
        booking.setStatus(
                BookingStatus.WAITING
        );

        return bookingRepository.save(booking);
    }
}