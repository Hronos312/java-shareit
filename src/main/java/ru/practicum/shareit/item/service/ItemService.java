package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemService {

    Item create(Long userId, Item item);

    Item update(Long userId, Long itemId, Item updatedItem);

    Item findById(Long itemId);

    Collection<Item> findAllByOwner(Long userId);

    Collection<Item> search(String text);

}
