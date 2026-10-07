package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService requestService;

    @Test
    void createShouldReturnCreatedRequest() throws Exception {
        ItemRequestDto input = new ItemRequestDto();
        input.setDescription("Нужна дрель");

        ItemRequestDto result = new ItemRequestDto();
        result.setId(1L);
        result.setDescription("Нужна дрель");
        result.setCreated(LocalDateTime.now());
        result.setItems(List.of());

        when(requestService.create(1L, input))
                .thenReturn(result);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description")
                        .value("Нужна дрель"))
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.items").isArray());

        verify(requestService).create(1L, input);
    }

    @Test
    void findAllByRequesterShouldReturnRequests() throws Exception {
        ItemRequestDto request = new ItemRequestDto();
        request.setId(1L);
        request.setDescription("Нужна дрель");
        request.setCreated(LocalDateTime.now());
        request.setItems(List.of());

        when(requestService.findAllByRequester(1L))
                .thenReturn(List.of(request));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description")
                        .value("Нужна дрель"));

        verify(requestService)
                .findAllByRequester(1L);
    }

    @Test
    void findAllByOthersShouldPassPagination() throws Exception {
        when(requestService.findAllByOthers(
                1L,
                5,
                10
        )).thenReturn(List.of());

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "5")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(requestService)
                .findAllByOthers(
                        1L,
                        5,
                        10
                );
    }

    @Test
    void findByIdShouldReturnRequest() throws Exception {
        ItemRequestDto request = new ItemRequestDto();
        request.setId(5L);
        request.setDescription("Нужен перфоратор");
        request.setCreated(LocalDateTime.now());
        request.setItems(List.of());

        when(requestService.findById(1L, 5L))
                .thenReturn(request);

        mockMvc.perform(get("/requests/5")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.description")
                        .value("Нужен перфоратор"));

        verify(requestService)
                .findById(1L, 5L);
    }
}