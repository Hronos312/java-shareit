package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private final ItemRequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestBody @Valid ItemRequestDto requestDto) {

        return requestClient.create(userId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> findAllByRequester(@RequestHeader("X-Sharer-User-Id") long userId) {
        return requestClient.findAllByRequester(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAllByOthers(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(defaultValue = "0")
            @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10")
            @Positive Integer size) {

        return requestClient.findAllByOthers(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findById(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable Long requestId) {
        return requestClient.findById(userId, requestId);
    }
}