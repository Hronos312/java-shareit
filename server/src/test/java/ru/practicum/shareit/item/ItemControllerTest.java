package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@Import(ErrorHandler.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void createShouldReturnCreatedItem() throws Exception {
        ItemDto input = new ItemDto();
        input.setName("Дрель");
        input.setDescription("Обычная дрель");
        input.setAvailable(true);

        ItemDto result = new ItemDto();
        result.setId(1L);
        result.setName("Дрель");
        result.setDescription("Обычная дрель");
        result.setAvailable(true);

        when(itemService.create(
                eq(1L),
                any(ItemDto.class)
        )).thenReturn(result);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.description")
                        .value("Обычная дрель"))
                .andExpect(jsonPath("$.available").value(true));

        verify(itemService)
                .create(eq(1L), any(ItemDto.class));
    }

    @Test
    void updateShouldReturnUpdatedItem() throws Exception {
        ItemDto input = new ItemDto();
        input.setName("Новая дрель");

        ItemDto result = new ItemDto();
        result.setId(5L);
        result.setName("Новая дрель");
        result.setDescription("Описание");
        result.setAvailable(true);

        when(itemService.update(
                eq(1L),
                eq(5L),
                any(ItemDto.class)
        )).thenReturn(result);

        mockMvc.perform(patch("/items/5")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name")
                        .value("Новая дрель"));

        verify(itemService)
                .update(
                        eq(1L),
                        eq(5L),
                        any(ItemDto.class)
                );
    }

    @Test
    void findByIdShouldReturnItem() throws Exception {
        ItemDto item = new ItemDto();
        item.setId(5L);
        item.setName("Дрель");
        item.setDescription("Описание");
        item.setAvailable(true);
        item.setComments(List.of());

        when(itemService.findById(1L, 5L))
                .thenReturn(item);

        mockMvc.perform(get("/items/5")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.description")
                        .value("Описание"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.comments").isArray());

        verify(itemService)
                .findById(1L, 5L);
    }

    @Test
    void findAllByOwnerShouldReturnItems() throws Exception {
        ItemDto first = new ItemDto();
        first.setId(1L);
        first.setName("Дрель");
        first.setDescription("Описание");
        first.setAvailable(true);

        ItemDto second = new ItemDto();
        second.setId(2L);
        second.setName("Молоток");
        second.setDescription("Описание");
        second.setAvailable(false);

        when(itemService.findAllByOwner(1L))
                .thenReturn(List.of(first, second));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(itemService)
                .findAllByOwner(1L);
    }

    @Test
    void searchShouldReturnFoundItems() throws Exception {
        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Электрическая дрель");
        item.setAvailable(true);

        when(itemService.search("дрель"))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name")
                        .value("Дрель"));

        verify(itemService).search("дрель");
    }

    @Test
    void addCommentShouldReturnCreatedComment()
            throws Exception {

        CommentDto input = new CommentDto();
        input.setText("Хорошая вещь");

        CommentDto result = new CommentDto();
        result.setId(10L);
        result.setText("Хорошая вещь");
        result.setAuthorName("Ivan");
        result.setCreated(LocalDateTime.now());

        when(itemService.addComment(
                eq(2L),
                eq(5L),
                any(CommentDto.class)
        )).thenReturn(result);

        mockMvc.perform(post("/items/5/comment")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.text")
                        .value("Хорошая вещь"))
                .andExpect(jsonPath("$.authorName")
                        .value("Ivan"))
                .andExpect(jsonPath("$.created").exists());

        verify(itemService)
                .addComment(
                        eq(2L),
                        eq(5L),
                        any(CommentDto.class)
                );
    }

    @Test
    void findByIdShouldReturnNotFound() throws Exception {
        when(itemService.findById(1L, 999L))
                .thenThrow(
                        new NotFoundException(
                                "Вещь с id 999 не найдена"
                        )
                );

        mockMvc.perform(get("/items/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Вещь с id 999 не найдена"));
    }

    @Test
    void addCommentShouldReturnBadRequestWhenNotAllowed()
            throws Exception {

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Комментарий");

        when(itemService.addComment(
                eq(2L),
                eq(5L),
                any(CommentDto.class)
        )).thenThrow(
                new ValidationException(
                        "Пользователь не может оставить комментарий к этой вещи"
                )
        );

        mockMvc.perform(post("/items/5/comment")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(commentDto)
                        ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Пользователь не может оставить комментарий к этой вещи"));
    }
}