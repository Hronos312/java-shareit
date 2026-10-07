package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.groups.Default;
import lombok.Data;

@Data
public class UserDto {

    public interface Create extends Default {
    }

    private Long id;

    @NotBlank(groups = Create.class)
    @Pattern(regexp = ".*\\S.*")
    private String name;

    @NotBlank(groups = Create.class)
    @Email
    @Pattern(regexp = ".*\\S.*")
    private String email;
}