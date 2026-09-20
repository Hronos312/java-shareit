package ru.practicum.shareit.item.service;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemService {

    Item create(Long userId, Item item);

    Item update(Long userId, Long itemId, Item updatedItem);

    ItemDto findById(Long userId, Long itemId);

    Collection<ItemDto> findAllByOwner(Long userId);

    Collection<Item> search(String text);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);

}
