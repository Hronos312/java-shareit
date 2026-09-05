package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ItemRepository {

    private final Map<Long, Item> items = new ConcurrentHashMap<>();
    private AtomicLong nextId = new AtomicLong(1);

    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(nextId.getAndIncrement());
        }

        items.put(item.getId(), item);
        return item;
    }

    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    public Collection<Item> findAll() {
        return new ArrayList<>(items.values());
    }
}