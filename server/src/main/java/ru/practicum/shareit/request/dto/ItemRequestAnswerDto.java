package ru.practicum.shareit.request.dto;

import lombok.Data;

@Data
public class ItemRequestAnswerDto {

    private Long id;
    private String name;
    private Long ownerId;

}