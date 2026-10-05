package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDto {

    public interface Create {
    }

    private Long id;

    private String name;

    @NotBlank(groups = Create.class)
    @Email
    private String email;
}