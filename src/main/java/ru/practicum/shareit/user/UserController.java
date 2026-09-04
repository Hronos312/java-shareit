package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    public UserDto create(@RequestBody UserDto userDto) {
        User user = userMapper.toUser(userDto);
        User createdUser = userService.create(user);

        return userMapper.toUserDto(createdUser);
    }

    @GetMapping("/{userId}")
    public UserDto findById(@PathVariable Long userId) {
        User user = userService.findById(userId);

        return userMapper.toUserDto(user);
    }

    @GetMapping
    public Collection<UserDto> findAll() {
        return userService.findAll().stream()
                .map(userMapper::toUserDto)
                .toList();
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Long userId, @RequestBody UserDto userDto) {
        User updatedUser = userMapper.toUser(userDto);
        User user = userService.update(userId, updatedUser);

        return userMapper.toUserDto(user);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        userService.delete(userId);
    }
}