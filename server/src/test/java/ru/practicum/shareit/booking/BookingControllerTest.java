package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@Import(ErrorHandler.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private BookingMapper bookingMapper;

    @Test
    void createShouldReturnBooking() throws Exception {
        BookingRequestDto request = new BookingRequestDto();
        request.setItemId(10L);
        request.setStart(LocalDateTime.now().plusHours(1));
        request.setEnd(LocalDateTime.now().plusHours(2));

        Booking booking = mock(Booking.class);

        BookingDto result = new BookingDto();
        result.setId(1L);
        result.setStart(request.getStart());
        result.setEnd(request.getEnd());
        result.setStatus(BookingStatus.WAITING);

        when(bookingService.create(eq(2L), any(BookingRequestDto.class)))
                .thenReturn(booking);

        when(bookingMapper.toBookingDto(booking))
                .thenReturn(result);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingService)
                .create(eq(2L), any(BookingRequestDto.class));

        verify(bookingMapper)
                .toBookingDto(booking);
    }

    @Test
    void approveShouldReturnApprovedBooking() throws Exception {
        Booking booking = mock(Booking.class);

        BookingDto result = new BookingDto();
        result.setId(1L);
        result.setStatus(BookingStatus.APPROVED);

        when(bookingService.approve(1L, 1L, true))
                .thenReturn(booking);

        when(bookingMapper.toBookingDto(booking))
                .thenReturn(result);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingService)
                .approve(1L, 1L, true);
    }

    @Test
    void findByIdShouldReturnBooking() throws Exception {
        Booking booking = mock(Booking.class);

        BookingDto result = new BookingDto();
        result.setId(5L);
        result.setStatus(BookingStatus.WAITING);

        when(bookingService.findById(2L, 5L))
                .thenReturn(booking);

        when(bookingMapper.toBookingDto(booking))
                .thenReturn(result);

        mockMvc.perform(get("/bookings/5")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingService)
                .findById(2L, 5L);
    }

    @Test
    void findAllByBookerShouldReturnBookings() throws Exception {
        Booking booking = mock(Booking.class);

        BookingDto dto = new BookingDto();
        dto.setId(1L);
        dto.setStatus(BookingStatus.WAITING);

        when(bookingService.findAllByBooker(
                2L,
                BookingState.ALL,
                0,
                10
        )).thenReturn(List.of(booking));

        when(bookingMapper.toBookingDto(booking))
                .thenReturn(dto);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(bookingService)
                .findAllByBooker(
                        2L,
                        BookingState.ALL,
                        0,
                        10
                );
    }

    @Test
    void findAllByOwnerShouldReturnBookings() throws Exception {
        Booking booking = mock(Booking.class);

        BookingDto dto = new BookingDto();
        dto.setId(1L);
        dto.setStatus(BookingStatus.APPROVED);

        when(bookingService.findAllByOwner(
                1L,
                BookingState.ALL,
                0,
                10
        )).thenReturn(List.of(booking));

        when(bookingMapper.toBookingDto(booking))
                .thenReturn(dto);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(bookingService)
                .findAllByOwner(
                        1L,
                        BookingState.ALL,
                        0,
                        10
                );
    }

    @Test
    void findAllShouldReturnBadRequestForUnknownState()
            throws Exception {

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void findByIdShouldReturnNotFound()
            throws Exception {

        when(bookingService.findById(1L, 999L))
                .thenThrow(
                        new NotFoundException(
                                "Бронирование не найдено"
                        )
                );

        mockMvc.perform(get("/bookings/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Бронирование не найдено"));
    }

    @Test
    void approveShouldReturnForbiddenForWrongUser()
            throws Exception {

        when(bookingService.approve(2L, 1L, true))
                .thenThrow(
                        new ForbiddenException(
                                "Подтвердить бронирование может только владелец вещи"
                        )
                );

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 2L)
                        .param("approved", "true"))
                .andExpect(status().isForbidden());
    }
}