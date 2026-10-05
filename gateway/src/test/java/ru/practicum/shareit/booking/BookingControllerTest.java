package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.ErrorHandler;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@Import(ErrorHandler.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void createShouldPassValidBookingToClient() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(
                5L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2)
        );

        when(bookingClient.create(
                eq(1L),
                any(BookItemRequestDto.class)
        )).thenReturn(ResponseEntity.ok().body(dto));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(bookingClient).create(
                eq(1L),
                any(BookItemRequestDto.class)
        );
    }

    @Test
    void createShouldRejectMissingStart() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(
                5L,
                null,
                LocalDateTime.now().plusHours(2)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void createShouldRejectMissingEnd() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(
                5L,
                LocalDateTime.now().plusHours(1),
                null
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void createShouldRejectStartInPast() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(
                5L,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(2)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void approveShouldPassParametersToClient() throws Exception {
        when(bookingClient.approve(
                1L,
                10L,
                true
        )).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/bookings/10")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk());

        verify(bookingClient).approve(
                1L,
                10L,
                true
        );
    }

    @Test
    void findAllShouldAcceptCaseInsensitiveState()
            throws Exception {

        when(bookingClient.findAllByBooker(
                1L,
                BookingState.WAITING,
                0,
                10
        )).thenReturn(ResponseEntity.ok().body(new Object[0]));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "waiting"))
                .andExpect(status().isOk());

        verify(bookingClient).findAllByBooker(
                1L,
                BookingState.WAITING,
                0,
                10
        );
    }

    @Test
    void findAllShouldRejectUnknownState()
            throws Exception {

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void findAllShouldRejectNegativeFrom()
            throws Exception {

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void findAllShouldRejectZeroSize()
            throws Exception {

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }
}