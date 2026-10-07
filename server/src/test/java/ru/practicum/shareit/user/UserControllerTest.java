package ru.practicum.shareit.user;

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
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

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
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @Test
    void createShouldReturnCreatedUser() throws Exception {
        UserDto input = new UserDto();
        input.setName("Ivan");
        input.setEmail("ivan@mail.ru");

        User user = mock(User.class);
        User savedUser = mock(User.class);

        UserDto result = new UserDto();
        result.setId(1L);
        result.setName("Ivan");
        result.setEmail("ivan@mail.ru");

        when(userMapper.toUser(any(UserDto.class)))
                .thenReturn(user);

        when(userService.create(user))
                .thenReturn(savedUser);

        when(userMapper.toUserDto(savedUser))
                .thenReturn(result);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@mail.ru"));

        verify(userService).create(user);
        verify(userMapper).toUserDto(savedUser);
    }

    @Test
    void findByIdShouldReturnUser() throws Exception {
        User user = mock(User.class);

        UserDto result = new UserDto();
        result.setId(5L);
        result.setName("Ivan");
        result.setEmail("ivan@mail.ru");

        when(userService.findById(5L))
                .thenReturn(user);

        when(userMapper.toUserDto(user))
                .thenReturn(result);

        mockMvc.perform(get("/users/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@mail.ru"));

        verify(userService).findById(5L);
    }

    @Test
    void findAllShouldReturnUsers() throws Exception {
        User first = mock(User.class);
        User second = mock(User.class);

        UserDto firstDto = new UserDto();
        firstDto.setId(1L);
        firstDto.setName("Ivan");
        firstDto.setEmail("ivan@mail.ru");

        UserDto secondDto = new UserDto();
        secondDto.setId(2L);
        secondDto.setName("Petr");
        secondDto.setEmail("petr@mail.ru");

        when(userService.findAll())
                .thenReturn(List.of(first, second));

        when(userMapper.toUserDto(first))
                .thenReturn(firstDto);

        when(userMapper.toUserDto(second))
                .thenReturn(secondDto);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(userService).findAll();
    }

    @Test
    void updateShouldReturnUpdatedUser() throws Exception {
        UserDto input = new UserDto();
        input.setName("New name");

        User updatedUser = mock(User.class);
        User savedUser = mock(User.class);

        UserDto result = new UserDto();
        result.setId(1L);
        result.setName("New name");
        result.setEmail("ivan@mail.ru");

        when(userMapper.toUser(any(UserDto.class)))
                .thenReturn(updatedUser);

        when(userService.update(1L, updatedUser))
                .thenReturn(savedUser);

        when(userMapper.toUserDto(savedUser))
                .thenReturn(result);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New name"))
                .andExpect(jsonPath("$.email").value("ivan@mail.ru"));

        verify(userService).update(1L, updatedUser);
    }

    @Test
    void deleteShouldCallService() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userService).delete(1L);
    }

    @Test
    void findByIdShouldReturnNotFound() throws Exception {
        when(userService.findById(999L))
                .thenThrow(
                        new NotFoundException(
                                "Пользователь с id 999 не найден"
                        )
                );

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Пользователь с id 999 не найден"));
    }
}