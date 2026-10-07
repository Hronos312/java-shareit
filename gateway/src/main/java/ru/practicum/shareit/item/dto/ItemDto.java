package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.groups.Default;
import lombok.Data;

@Data
public class ItemDto {

    public interface Create {
    }

    private Long id;

    @NotNull(groups = Create.class)
    @Pattern(
            regexp = ".*\\S.*",
            groups = {Create.class, Default.class}
    )
    private String name;

    @NotNull(groups = Create.class)
    @Pattern(
            regexp = ".*\\S.*",
            groups = {Create.class, Default.class}
    )
    private String description;

    @NotNull(groups = Create.class)
    private Boolean available;

    private Long requestId;

}