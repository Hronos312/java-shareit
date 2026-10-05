package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient requestClient;

    @Test
    void createShouldPassValidRequestToClient()
            throws Exception {

        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Нужна дрель");

        when(requestClient.create(
                eq(1L),
                any(ItemRequestDto.class)
        )).thenReturn(
                ResponseEntity.ok()
                        .body(dto)
        );

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(
                                objectMapper.writeValueAsString(dto)
                        ))
                .andExpect(status().isOk());

        verify(requestClient).create(
                eq(1L),
                any(ItemRequestDto.class)
        );
    }

    @Test
    void createShouldReturnBadRequestWhenDescriptionBlank()
            throws Exception {

        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("");

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(
                                objectMapper.writeValueAsString(dto)
                        ))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(requestClient);
    }

    @Test
    void findAllByRequesterShouldCallClient()
            throws Exception {

        when(requestClient.findAllByRequester(1L))
                .thenReturn(
                        ResponseEntity.ok()
                                .body(new Object[0])
                );

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(requestClient)
                .findAllByRequester(1L);
    }

    @Test
    void findAllByOthersShouldPassPagination()
            throws Exception {

        when(requestClient.findAllByOthers(
                1L,
                5,
                10
        )).thenReturn(
                ResponseEntity.ok()
                        .body(new Object[0])
        );

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "5")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(requestClient)
                .findAllByOthers(
                        1L,
                        5,
                        10
                );
    }

    @Test
    void findAllByOthersShouldRejectNegativeFrom()
            throws Exception {

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(requestClient);
    }

    @Test
    void findAllByOthersShouldRejectZeroSize()
            throws Exception {

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(requestClient);
    }

    @Test
    void findByIdShouldCallClient()
            throws Exception {

        when(requestClient.findById(1L, 5L))
                .thenReturn(
                        ResponseEntity.ok()
                                .body(new Object[0])
                );

        mockMvc.perform(get("/requests/5")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(requestClient)
                .findById(1L, 5L);
    }
}