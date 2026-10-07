package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@Import(ErrorHandler.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    void createShouldPassValidItemToClient() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Обычная дрель");
        itemDto.setAvailable(true);

        when(itemClient.create(eq(1L), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok().body(itemDto));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());

        verify(itemClient).create(
                eq(1L),
                any(ItemDto.class)
        );
    }

    @Test
    void createShouldRejectBlankName() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("   ");
        itemDto.setDescription("Описание");
        itemDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void createShouldRejectMissingDescription() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void createShouldRejectMissingAvailable() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Описание");

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void updateShouldAllowPartialUpdate() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Новое название");

        when(itemClient.update(
                eq(1L),
                eq(5L),
                any(ItemDto.class)
        )).thenReturn(ResponseEntity.ok().body(itemDto));

        mockMvc.perform(patch("/items/5")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());

        verify(itemClient).update(
                eq(1L),
                eq(5L),
                any(ItemDto.class)
        );
    }

    @Test
    void addCommentShouldRejectBlankText() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("");

        mockMvc.perform(post("/items/5/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void findByIdShouldCallClient() throws Exception {
        when(itemClient.findById(1L, 5L))
                .thenReturn(
                        ResponseEntity.ok(
                                java.util.Map.of(
                                        "id", 5L,
                                        "name", "Дрель",
                                        "description", "Описание",
                                        "available", true
                                )
                        )
                );

        mockMvc.perform(get("/items/5")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Дрель"));

        verify(itemClient)
                .findById(1L, 5L);
    }

    @Test
    void findAllByOwnerShouldCallClient() throws Exception {
        when(itemClient.findAllByOwner(1L))
                .thenReturn(
                        ResponseEntity.ok(
                                java.util.List.of(
                                        java.util.Map.of(
                                                "id", 1L,
                                                "name", "Дрель"
                                        ),
                                        java.util.Map.of(
                                                "id", 2L,
                                                "name", "Молоток"
                                        )
                                )
                        )
                );

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(itemClient)
                .findAllByOwner(1L);
    }

    @Test
    void searchShouldCallClient() throws Exception {
        when(itemClient.search("дрель"))
                .thenReturn(
                        ResponseEntity.ok(
                                java.util.List.of(
                                        java.util.Map.of(
                                                "id", 1L,
                                                "name", "Дрель"
                                        )
                                )
                        )
                );

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name")
                        .value("Дрель"));

        verify(itemClient)
                .search("дрель");
    }
}