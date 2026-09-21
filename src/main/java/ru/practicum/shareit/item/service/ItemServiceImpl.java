package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final ItemMapper itemMapper;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto) {
        Item item = itemMapper.toItem(itemDto);

        validateItemForCreate(item);

        User owner = userService.findById(userId);

        item.setId(null);
        item.setOwner(owner);

        Item savedItem = itemRepository.save(item);

        return itemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto findById(Long userId, Long itemId) {
        Item item = getItemById(itemId);

        return toItemDtoWithBookings(item, userId);
    }

    @Override
    public Collection<ItemDto> findAllByOwner(Long userId) {
        userService.findById(userId);

        List<Item> items = itemRepository.findAllByOwnerId(userId);

        if (items.isEmpty()) {
            return List.of();
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();

        List<Comment> comments = commentRepository.findAllByItemIdInOrderByCreatedAsc(itemIds);

        List<Booking> bookings = bookingRepository.findByOwnerAndStatus(userId, BookingStatus.APPROVED);

        Map<Long, List<Comment>> commentsByItem = comments.stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        Map<Long, List<Booking>> bookingsByItem = bookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> toItemDtoWithPreloadedData(
                        item,
                        commentsByItem.getOrDefault(item.getId(), List.of()),
                        bookingsByItem.getOrDefault(item.getId(), List.of()),
                        now
                ))
                .toList();
    }

    @Override
    public Collection<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository
                .findAllByAvailableTrueAndNameContainingIgnoreCaseOrAvailableTrueAndDescriptionContainingIgnoreCase(text, text)
                .stream()
                .map(itemMapper::toItemDto)
                .toList();
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Item item = getItemById(itemId);
        Item updatedItem = itemMapper.toItem(itemDto);

        if (!item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Пользователь с id " + userId + " не является владельцем вещи");
        }

        validateItemForUpdate(updatedItem);

        if (updatedItem.getName() != null) {
            item.setName(updatedItem.getName());
        }

        if (updatedItem.getDescription() != null) {
            item.setDescription(updatedItem.getDescription());
        }

        if (updatedItem.getAvailable() != null) {
            item.setAvailable(updatedItem.getAvailable());
        }

        return itemMapper.toItemDto(itemRepository.save(item));
    }

    private void validateItemForCreate(Item item) {
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ValidationException("Название вещи не может быть пустым");
        }

        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ValidationException("Описание вещи не может быть пустым");
        }

        if (item.getAvailable() == null) {
            throw new ValidationException("Необходимо указать доступность вещи для аренды");
        }
    }

    private void validateItemForUpdate(Item item) {
        if (item.getName() != null && item.getName().isBlank()) {
            throw new ValidationException("Название вещи не может быть пустым");
        }

        if (item.getDescription() != null && item.getDescription().isBlank()) {
            throw new ValidationException("Описание вещи не может быть пустым");
        }
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = userService.findById(userId);
        Item item = getItemById(itemId);

        boolean hasCompletedBooking =
                bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                        userId,
                        itemId,
                        BookingStatus.APPROVED,
                        LocalDateTime.now()
                );

        if (!hasCompletedBooking) {
            throw new ValidationException("Пользователь не может оставить комментарий к этой вещи");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        return commentMapper.toCommentDto(
                commentRepository.save(comment)
        );
    }

    private Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));
    }

    private ItemDto toItemDtoWithBookings(Item item, Long userId) {
        ItemDto dto = itemMapper.toItemDto(item);

        List<CommentDto> comments = commentRepository
                .findAllByItemIdOrderByCreatedAsc(item.getId())
                .stream()
                .map(commentMapper::toCommentDto)
                .toList();

        dto.setComments(comments);

        if (!item.getOwner().getId().equals(userId)) {
            return dto;
        }

        LocalDateTime now = LocalDateTime.now();

        bookingRepository.findFirstByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(item.getId(), BookingStatus.APPROVED, now)
                .map(bookingMapper::toBookingShortDto)
                .ifPresent(dto::setLastBooking);

        bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(item.getId(), BookingStatus.APPROVED, now)
                .map(bookingMapper::toBookingShortDto)
                .ifPresent(dto::setNextBooking);

        return dto;
    }

    private ItemDto toItemDtoWithPreloadedData(Item item, List<Comment> comments, List<Booking> bookings, LocalDateTime now) {
        ItemDto dto = itemMapper.toItemDto(item);

        dto.setComments(comments.stream()
                .map(commentMapper::toCommentDto)
                .toList()
        );

        bookings.stream()
                .filter(booking -> !booking.getStart().isAfter(now))
                .max(Comparator.comparing(Booking::getStart))
                .map(bookingMapper::toBookingShortDto)
                .ifPresent(dto::setLastBooking);

        bookings.stream()
                .filter(booking -> booking.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart))
                .map(bookingMapper::toBookingShortDto)
                .ifPresent(dto::setNextBooking);

        return dto;
    }

}
