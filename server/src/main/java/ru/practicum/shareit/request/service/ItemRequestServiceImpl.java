package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.util.OffsetPageRequest;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper requestMapper;
    private final UserService userService;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestDto requestDto) {

        User requester = userService.findById(userId);

        ItemRequest request = new ItemRequest();
        request.setDescription(requestDto.getDescription());
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = requestRepository.save(request);

        return requestMapper.toDto(savedRequest, List.of());
    }

    @Override
    public Collection<ItemRequestDto> findAllByRequester(Long userId) {

        userService.findById(userId);

        List<ItemRequest> requests = requestRepository.findAllByRequesterIdOrderByCreatedDesc(userId);

        return mapRequests(requests);
    }

    @Override
    public Collection<ItemRequestDto> findAllByOthers(Long userId, Integer from, Integer size) {

        userService.findById(userId);

        Pageable pageable = new OffsetPageRequest(from, size, Sort.unsorted());

        List<ItemRequest> requests = requestRepository.findAllByRequesterIdNotOrderByCreatedDesc(userId, pageable);

        return mapRequests(requests);
    }

    @Override
    public ItemRequestDto findById(Long userId, Long requestId) {

        userService.findById(userId);

        ItemRequest request = requestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("Запрос с id " + requestId + " не найден"));

        List<Item> items = itemRepository.findAllByRequestIdIn(List.of(requestId));

        return requestMapper.toDto(request, items);
    }

    private Collection<ItemRequestDto> mapRequests(List<ItemRequest> requests) {

        if (requests.isEmpty()) {
            return List.of();
        }

        List<Long> requestIds = requests.stream().map(ItemRequest::getId).toList();

        List<Item> items = itemRepository.findAllByRequestIdIn(requestIds);

        Map<Long, List<Item>> itemsByRequest = items.stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return requests.stream()
                .map(request -> requestMapper.toDto(request, itemsByRequest.getOrDefault(request.getId(), List.of())))
                .toList();
    }
}