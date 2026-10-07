package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Collection;

public interface ItemRequestService {

    ItemRequestDto create(Long userId, ItemRequestDto requestDto);

    Collection<ItemRequestDto> findAllByRequester(Long userId);

    Collection<ItemRequestDto> findAllByOthers(Long userId, Integer from, Integer size);

    ItemRequestDto findById(Long userId, Long requestId);

}