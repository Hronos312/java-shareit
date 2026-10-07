package ru.practicum.shareit.client;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class GatewayClientsTest {

    private static final String SERVER_URL =
            "http://localhost:9090";

    @Test
    void userClientShouldSendAllRequests() {
        MockServerRestTemplateCustomizer customizer =
                new MockServerRestTemplateCustomizer();

        UserClient client = new UserClient(
                SERVER_URL,
                new RestTemplateBuilder(customizer)
        );

        MockRestServiceServer server =
                customizer.getServer();

        UserDto dto = new UserDto();
        dto.setName("Ivan");
        dto.setEmail("ivan@mail.ru");

        server.expect(requestTo(SERVER_URL + "/users"))
                .andExpect(method(POST))
                .andRespond(withSuccess(
                        "{}",
                        MediaType.APPLICATION_JSON
                ));

        server.expect(requestTo(SERVER_URL + "/users/1"))
                .andExpect(method(PATCH))
                .andRespond(withSuccess(
                        "{}",
                        MediaType.APPLICATION_JSON
                ));

        server.expect(requestTo(SERVER_URL + "/users/1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(
                        "{}",
                        MediaType.APPLICATION_JSON
                ));

        server.expect(requestTo(SERVER_URL + "/users"))
                .andExpect(method(GET))
                .andRespond(withSuccess(
                        "[]",
                        MediaType.APPLICATION_JSON
                ));

        server.expect(requestTo(SERVER_URL + "/users/1"))
                .andExpect(method(DELETE))
                .andRespond(withSuccess());

        client.create(dto);
        client.update(1L, dto);
        client.findById(1L);
        client.findAll();
        client.delete(1L);

        server.verify();
    }

    @Test
    void userClientShouldForwardServerError() {
        MockServerRestTemplateCustomizer customizer =
                new MockServerRestTemplateCustomizer();

        UserClient client = new UserClient(
                SERVER_URL,
                new RestTemplateBuilder(customizer)
        );

        MockRestServiceServer server =
                customizer.getServer();

        server.expect(
                        requestTo(
                                SERVER_URL + "/users/999"
                        )
                )
                .andExpect(method(GET))
                .andRespond(
                        withStatus(HttpStatus.NOT_FOUND)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .body("{\"error\":\"User not found\"}")
                );

        var response = client.findById(999L);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        server.verify();
    }

    @Test
    void itemClientShouldSendAllRequests() {
        MockServerRestTemplateCustomizer customizer =
                new MockServerRestTemplateCustomizer();

        ItemClient client = new ItemClient(
                SERVER_URL,
                new RestTemplateBuilder(customizer)
        );

        MockRestServiceServer server =
                customizer.getServer();

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Drill");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Good");

        server.expect(requestTo(SERVER_URL + "/items"))
                .andExpect(method(POST))
                .andExpect(header(
                        "X-Sharer-User-Id",
                        "1"
                ))
                .andRespond(withSuccess(
                        "{}",
                        MediaType.APPLICATION_JSON
                ));

        server.expect(requestTo(SERVER_URL + "/items/5"))
                .andExpect(method(PATCH))
                .andRespond(withSuccess(
                        "{}",
                        MediaType.APPLICATION_JSON
                ));

        server.expect(requestTo(SERVER_URL + "/items/5"))
                .andExpect(method(GET))
                .andRespond(withSuccess(
                        "{}",
                        MediaType.APPLICATION_JSON
                ));

        server.expect(requestTo(SERVER_URL + "/items"))
                .andExpect(method(GET))
                .andRespond(withSuccess(
                        "[]",
                        MediaType.APPLICATION_JSON
                ));

        server.expect(requestTo(
                        SERVER_URL + "/items/search?text=drill"
                ))
                .andExpect(method(GET))
                .andRespond(withSuccess(
                        "[]",
                        MediaType.APPLICATION_JSON
                ));

        server.expect(requestTo(
                        SERVER_URL + "/items/5/comment"
                ))
                .andExpect(method(POST))
                .andRespond(withSuccess(
                        "{}",
                        MediaType.APPLICATION_JSON
                ));

        client.create(1L, itemDto);
        client.update(1L, 5L, itemDto);
        client.findById(1L, 5L);
        client.findAllByOwner(1L);
        client.search("drill");
        client.addComment(1L, 5L, commentDto);

        server.verify();
    }

    @Test
    void bookingClientShouldSendAllRequests() {
        MockServerRestTemplateCustomizer customizer =
                new MockServerRestTemplateCustomizer();

        BookingClient client = new BookingClient(
                SERVER_URL,
                new RestTemplateBuilder(customizer)
        );

        MockRestServiceServer server =
                customizer.getServer();

        BookItemRequestDto request =
                new BookItemRequestDto(
                        5L,
                        LocalDateTime.now().plusHours(1),
                        LocalDateTime.now().plusHours(2)
                );

        server.expect(requestTo(SERVER_URL + "/bookings"))
                .andExpect(method(POST))
                .andRespond(withSuccess(
                        "{}",
                        MediaType.APPLICATION_JSON
                ));

        server.expect(requestTo(
                        SERVER_URL
                                + "/bookings/10"
                                + "?approved=true"
                ))
                .andExpect(method(PATCH))
                .andRespond(withSuccess(
                        "{}",
                        MediaType.APPLICATION_JSON
                ));

        server.expect(requestTo(
                        SERVER_URL + "/bookings/10"
                ))
                .andExpect(method(GET))
                .andRespond(withSuccess(
                        "{}",
                        MediaType.APPLICATION_JSON
                ));

        server.expect(requestTo(
                        SERVER_URL
                                + "/bookings"
                                + "?state=ALL"
                                + "&from=0"
                                + "&size=10"
                ))
                .andExpect(method(GET))
                .andRespond(withSuccess(
                        "[]",
                        MediaType.APPLICATION_JSON
                ));

        server.expect(requestTo(
                        SERVER_URL
                                + "/bookings/owner"
                                + "?state=ALL"
                                + "&from=0"
                                + "&size=10"
                ))
                .andExpect(method(GET))
                .andRespond(withSuccess(
                        "[]",
                        MediaType.APPLICATION_JSON
                ));

        client.create(1L, request);
        client.approve(1L, 10L, true);
        client.findById(1L, 10L);
        client.findAllByBooker(
                1L,
                BookingState.ALL,
                0,
                10
        );
        client.findAllByOwner(
                1L,
                BookingState.ALL,
                0,
                10
        );

        server.verify();
    }

    @Test
    void itemRequestClientShouldSendAllRequests() {
        MockServerRestTemplateCustomizer customizer =
                new MockServerRestTemplateCustomizer();

        ItemRequestClient client =
                new ItemRequestClient(
                        SERVER_URL,
                        new RestTemplateBuilder(customizer)
                );

        MockRestServiceServer server =
                customizer.getServer();

        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need drill");

        server.expect(requestTo(SERVER_URL + "/requests"))
                .andExpect(method(POST))
                .andRespond(withSuccess(
                        "{}",
                        MediaType.APPLICATION_JSON
                ));

        server.expect(requestTo(SERVER_URL + "/requests"))
                .andExpect(method(GET))
                .andRespond(withSuccess(
                        "[]",
                        MediaType.APPLICATION_JSON
                ));

        server.expect(requestTo(
                        SERVER_URL
                                + "/requests/all"
                                + "?from=0"
                                + "&size=10"
                ))
                .andExpect(method(GET))
                .andRespond(withSuccess(
                        "[]",
                        MediaType.APPLICATION_JSON
                ));

        server.expect(requestTo(
                        SERVER_URL + "/requests/5"
                ))
                .andExpect(method(GET))
                .andRespond(withSuccess(
                        "{}",
                        MediaType.APPLICATION_JSON
                ));

        client.create(1L, dto);
        client.findAllByRequester(1L);
        client.findAllByOthers(
                1L,
                0,
                10
        );
        client.findById(1L, 5L);

        server.verify();
    }
}