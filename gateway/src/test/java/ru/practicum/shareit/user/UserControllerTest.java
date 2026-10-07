package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(ErrorHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @Test
    void createShouldPassValidUserToClient() throws Exception {
        UserDto dto = new UserDto();
        dto.setName("Ivan");
        dto.setEmail("ivan@mail.ru");

        when(userClient.create(any(UserDto.class)))
                .thenReturn(
                        ResponseEntity.ok(
                                Map.of(
                                        "id", 1L,
                                        "name", "Ivan",
                                        "email", "ivan@mail.ru"
                                )
                        )
                );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@mail.ru"));

        verify(userClient)
                .create(any(UserDto.class));
    }

    @Test
    void updateShouldPassUserToClient() throws Exception {
        UserDto dto = new UserDto();
        dto.setName("New name");

        when(userClient.update(
                eq(1L),
                any(UserDto.class)
        )).thenReturn(
                ResponseEntity.ok(
                        Map.of(
                                "id", 1L,
                                "name", "New name",
                                "email", "ivan@mail.ru"
                        )
                )
        );

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("New name"));

        verify(userClient)
                .update(
                        eq(1L),
                        any(UserDto.class)
                );
    }

    @Test
    void findByIdShouldCallClient() throws Exception {
        when(userClient.findById(1L))
                .thenReturn(
                        ResponseEntity.ok(
                                Map.of(
                                        "id", 1L,
                                        "name", "Ivan",
                                        "email", "ivan@mail.ru"
                                )
                        )
                );

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(userClient)
                .findById(1L);
    }

    @Test
    void findAllShouldCallClient() throws Exception {
        when(userClient.findAll())
                .thenReturn(
                        ResponseEntity.ok(
                                List.of(
                                        Map.of(
                                                "id", 1L,
                                                "name", "Ivan",
                                                "email", "ivan@mail.ru"
                                        )
                                )
                        )
                );

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(userClient)
                .findAll();
    }

    @Test
    void deleteShouldCallClient() throws Exception {
        when(userClient.delete(1L))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userClient)
                .delete(1L);
    }

    @Test
    void createShouldRejectBlankName() throws Exception {
        UserDto dto = new UserDto();
        dto.setName("");
        dto.setEmail("ivan@mail.ru");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    void createShouldRejectInvalidEmail() throws Exception {
        UserDto dto = new UserDto();
        dto.setName("Ivan");
        dto.setEmail("abc");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    void updateShouldRejectInvalidEmail() throws Exception {
        UserDto dto = new UserDto();
        dto.setEmail("abc");

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    void updateShouldAllowPartialUpdate() throws Exception {
        UserDto dto = new UserDto();
        dto.setName("Only name");

        when(userClient.update(
                eq(1L),
                any(UserDto.class)
        )).thenReturn(
                ResponseEntity.ok(
                        Map.of(
                                "id", 1L,
                                "name", "Only name",
                                "email", "ivan@mail.ru"
                        )
                )
        );

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(userClient)
                .update(
                        eq(1L),
                        any(UserDto.class)
                );
    }
}