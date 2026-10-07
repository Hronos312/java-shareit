package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingUserDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestAnswerDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class DtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> bookingJson;

    @Autowired
    private JacksonTester<BookingRequestDto> bookingRequestJson;

    @Autowired
    private JacksonTester<ItemDto> itemJson;

    @Autowired
    private JacksonTester<ItemRequestDto> requestJson;

    @Test
    void bookingDtoShouldSerializeNestedObjects() throws Exception {
        BookingItemDto item = new BookingItemDto();
        item.setId(10L);
        item.setName("Дрель");

        BookingUserDto booker = new BookingUserDto();
        booker.setId(20L);
        booker.setName("Ivan");

        BookingDto dto = new BookingDto();
        dto.setId(1L);
        dto.setStart(
                LocalDateTime.of(2026, 10, 7, 12, 0)
        );
        dto.setEnd(
                LocalDateTime.of(2026, 10, 7, 13, 0)
        );
        dto.setItem(item);
        dto.setBooker(booker);
        dto.setStatus(BookingStatus.WAITING);

        var content = bookingJson.write(dto);

        assertThat(content)
                .extractingJsonPathNumberValue("@.id")
                .isEqualTo(1);

        assertThat(content)
                .extractingJsonPathStringValue("@.status")
                .isEqualTo("WAITING");

        assertThat(content)
                .extractingJsonPathNumberValue("@.item.id")
                .isEqualTo(10);

        assertThat(content)
                .extractingJsonPathStringValue("@.item.name")
                .isEqualTo("Дрель");

        assertThat(content)
                .extractingJsonPathNumberValue("@.booker.id")
                .isEqualTo(20);
    }

    @Test
    void bookingRequestDtoShouldDeserializeDates()
            throws Exception {

        String json =
                "{\"itemId\":10,"
                        + "\"start\":\"2026-10-07T12:00:00\","
                        + "\"end\":\"2026-10-07T14:00:00\"}";

        BookingRequestDto dto =
                bookingRequestJson.parseObject(json);

        assertThat(dto.getItemId())
                .isEqualTo(10L);

        assertThat(dto.getStart())
                .isEqualTo(
                        LocalDateTime.of(
                                2026, 10, 7, 12, 0
                        )
                );

        assertThat(dto.getEnd())
                .isEqualTo(
                        LocalDateTime.of(
                                2026, 10, 7, 14, 0
                        )
                );
    }

    @Test
    void itemDtoShouldSerializeBookingsAndComments()
            throws Exception {

        var lastBooking =
                new ru.practicum.shareit.booking.dto.BookingShortDto();

        lastBooking.setId(100L);
        lastBooking.setBookerId(2L);

        var nextBooking =
                new ru.practicum.shareit.booking.dto.BookingShortDto();

        nextBooking.setId(101L);
        nextBooking.setBookerId(3L);

        CommentDto comment = new CommentDto();
        comment.setId(50L);
        comment.setText("Хорошая вещь");
        comment.setAuthorName("Ivan");
        comment.setCreated(
                LocalDateTime.of(
                        2026, 10, 7, 15, 0
                )
        );

        ItemDto dto = new ItemDto();
        dto.setId(5L);
        dto.setName("Дрель");
        dto.setDescription("Описание");
        dto.setAvailable(true);
        dto.setRequestId(7L);
        dto.setLastBooking(lastBooking);
        dto.setNextBooking(nextBooking);
        dto.setComments(List.of(comment));

        var content = itemJson.write(dto);

        assertThat(content)
                .extractingJsonPathNumberValue("@.id")
                .isEqualTo(5);

        assertThat(content)
                .extractingJsonPathNumberValue(
                        "@.lastBooking.id"
                )
                .isEqualTo(100);

        assertThat(content)
                .extractingJsonPathNumberValue(
                        "@.nextBooking.id"
                )
                .isEqualTo(101);

        assertThat(content)
                .extractingJsonPathStringValue(
                        "@.comments[0].text"
                )
                .isEqualTo("Хорошая вещь");

        assertThat(content)
                .extractingJsonPathStringValue(
                        "@.comments[0].authorName"
                )
                .isEqualTo("Ivan");
    }

    @Test
    void itemRequestDtoShouldSerializeAnswerItems()
            throws Exception {

        ItemRequestAnswerDto answer =
                new ItemRequestAnswerDto();

        answer.setId(15L);
        answer.setName("Дрель Bosch");
        answer.setOwnerId(2L);

        ItemRequestDto dto =
                new ItemRequestDto();

        dto.setId(10L);
        dto.setDescription("Нужна дрель");
        dto.setCreated(
                LocalDateTime.of(
                        2026, 10, 7, 10, 0
                )
        );
        dto.setItems(List.of(answer));

        var content = requestJson.write(dto);

        assertThat(content)
                .extractingJsonPathNumberValue("@.id")
                .isEqualTo(10);

        assertThat(content)
                .extractingJsonPathStringValue("@.description")
                .isEqualTo("Нужна дрель");

        assertThat(content)
                .extractingJsonPathNumberValue(
                        "@.items[0].id"
                )
                .isEqualTo(15);

        assertThat(content)
                .extractingJsonPathStringValue(
                        "@.items[0].name"
                )
                .isEqualTo("Дрель Bosch");

        assertThat(content)
                .extractingJsonPathNumberValue(
                        "@.items[0].ownerId"
                )
                .isEqualTo(2);
    }
}