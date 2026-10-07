package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {

    @Test
    void bookingMapperShouldMapFullAndShortDto() {
        User booker = new User();
        booker.setId(2L);
        booker.setName("Ivan");

        Item item = new Item();
        item.setId(3L);
        item.setName("Дрель");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.of(2026, 10, 7, 12, 0));
        booking.setEnd(LocalDateTime.of(2026, 10, 7, 13, 0));
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.APPROVED);

        BookingMapper mapper = new BookingMapper();

        BookingDto dto = mapper.toBookingDto(booking);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(booking.getStart());
        assertThat(dto.getEnd()).isEqualTo(booking.getEnd());
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.APPROVED);

        assertThat(dto.getItem().getId()).isEqualTo(3L);
        assertThat(dto.getItem().getName()).isEqualTo("Дрель");

        assertThat(dto.getBooker().getId()).isEqualTo(2L);
        assertThat(dto.getBooker().getName()).isEqualTo("Ivan");

        BookingShortDto shortDto =
                mapper.toBookingShortDto(booking);

        assertThat(shortDto.getId()).isEqualTo(1L);
        assertThat(shortDto.getBookerId()).isEqualTo(2L);
    }

    @Test
    void userMapperShouldMapBothDirections() {
        User user = new User();
        user.setId(1L);
        user.setName("Ivan");
        user.setEmail("ivan@mail.ru");

        UserMapper mapper = new UserMapper();

        UserDto dto = mapper.toUserDto(user);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Ivan");
        assertThat(dto.getEmail()).isEqualTo("ivan@mail.ru");

        User mappedUser = mapper.toUser(dto);

        assertThat(mappedUser.getId()).isEqualTo(1L);
        assertThat(mappedUser.getName()).isEqualTo("Ivan");
        assertThat(mappedUser.getEmail()).isEqualTo("ivan@mail.ru");
    }

    @Test
    void itemMapperShouldMapBothDirectionsWithRequest() {
        ItemRequest request = new ItemRequest();
        request.setId(10L);

        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Описание");
        item.setAvailable(true);
        item.setRequest(request);

        ItemMapper mapper = new ItemMapper();

        ItemDto dto = mapper.toItemDto(item);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getDescription()).isEqualTo("Описание");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isEqualTo(10L);

        Item mappedItem = mapper.toItem(dto);

        assertThat(mappedItem.getId()).isEqualTo(1L);
        assertThat(mappedItem.getName()).isEqualTo("Дрель");
        assertThat(mappedItem.getDescription()).isEqualTo("Описание");
        assertThat(mappedItem.getAvailable()).isTrue();
    }

    @Test
    void itemMapperShouldMapNullRequest() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Описание");
        item.setAvailable(true);

        ItemDto dto =
                new ItemMapper().toItemDto(item);

        assertThat(dto.getRequestId()).isNull();
    }

    @Test
    void commentMapperShouldMapComment() {
        User author = new User();
        author.setName("Ivan");

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Хорошая вещь");
        comment.setAuthor(author);
        comment.setCreated(
                LocalDateTime.of(2026, 10, 7, 12, 0)
        );

        CommentDto dto =
                new CommentMapper().toCommentDto(comment);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getText()).isEqualTo("Хорошая вещь");
        assertThat(dto.getAuthorName()).isEqualTo("Ivan");
        assertThat(dto.getCreated())
                .isEqualTo(comment.getCreated());
    }
}