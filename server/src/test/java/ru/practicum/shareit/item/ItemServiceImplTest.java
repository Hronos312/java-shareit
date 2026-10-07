package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
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
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private UserService userService;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    @Test
    void createShouldLinkItemToRequest() {
        User owner = new User();
        owner.setId(1L);

        ItemRequest request = new ItemRequest();
        request.setId(10L);

        ItemDto input = new ItemDto();
        input.setName("Дрель");
        input.setDescription("Хорошая дрель");
        input.setAvailable(true);
        input.setRequestId(10L);

        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Хорошая дрель");
        item.setAvailable(true);

        ItemDto resultDto = new ItemDto();
        resultDto.setId(20L);
        resultDto.setName("Дрель");
        resultDto.setDescription("Хорошая дрель");
        resultDto.setAvailable(true);
        resultDto.setRequestId(10L);

        when(itemMapper.toItem(input))
                .thenReturn(item);

        when(userService.findById(1L))
                .thenReturn(owner);

        when(requestRepository.findById(10L))
                .thenReturn(Optional.of(request));

        when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocation -> {
                    Item savedItem = invocation.getArgument(0);
                    savedItem.setId(20L);
                    return savedItem;
                });

        when(itemMapper.toItemDto(any(Item.class)))
                .thenReturn(resultDto);

        ItemDto result =
                itemService.create(1L, input);

        assertThat(result.getId()).isEqualTo(20L);
        assertThat(result.getRequestId()).isEqualTo(10L);

        ArgumentCaptor<Item> captor =
                ArgumentCaptor.forClass(Item.class);

        verify(itemRepository).save(captor.capture());

        Item savedItem = captor.getValue();

        assertThat(savedItem.getOwner()).isEqualTo(owner);
        assertThat(savedItem.getRequest()).isEqualTo(request);
        assertThat(savedItem.getRequest().getId())
                .isEqualTo(10L);
    }

    @Test
    void createWithoutRequestShouldSaveItemWithoutRequest() {
        User owner = new User();
        owner.setId(1L);

        ItemDto input = new ItemDto();
        input.setName("Дрель");
        input.setDescription("Хорошая дрель");
        input.setAvailable(true);

        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Хорошая дрель");
        item.setAvailable(true);

        ItemDto resultDto = new ItemDto();
        resultDto.setId(20L);
        resultDto.setName("Дрель");

        when(itemMapper.toItem(input))
                .thenReturn(item);

        when(userService.findById(1L))
                .thenReturn(owner);

        when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        when(itemMapper.toItemDto(any(Item.class)))
                .thenReturn(resultDto);

        itemService.create(1L, input);

        ArgumentCaptor<Item> captor =
                ArgumentCaptor.forClass(Item.class);

        verify(itemRepository).save(captor.capture());

        assertThat(captor.getValue().getRequest())
                .isNull();

        verifyNoInteractions(requestRepository);
    }

    @Test
    void createShouldThrowWhenRequestDoesNotExist() {
        User owner = new User();
        owner.setId(1L);

        ItemDto input = new ItemDto();
        input.setName("Дрель");
        input.setDescription("Хорошая дрель");
        input.setAvailable(true);
        input.setRequestId(999L);

        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Хорошая дрель");
        item.setAvailable(true);

        when(itemMapper.toItem(input))
                .thenReturn(item);

        when(userService.findById(1L))
                .thenReturn(owner);

        when(requestRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> itemService.create(1L, input)
        )
                .isInstanceOf(NotFoundException.class);

        verify(itemRepository, never())
                .save(any(Item.class));
    }

    @Test
    void findByIdShouldReturnItemForNonOwnerWithoutBookings() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setId(5L);
        item.setOwner(owner);

        ItemDto dto = new ItemDto();
        dto.setId(5L);
        dto.setName("Дрель");

        Comment comment = new Comment();
        comment.setId(10L);
        comment.setItem(item);

        CommentDto commentDto = new CommentDto();
        commentDto.setId(10L);
        commentDto.setText("Хорошая вещь");

        when(itemRepository.findById(5L))
                .thenReturn(Optional.of(item));

        when(itemMapper.toItemDto(item))
                .thenReturn(dto);

        when(commentRepository.findAllByItemIdOrderByCreatedAsc(5L))
                .thenReturn(List.of(comment));

        when(commentMapper.toCommentDto(comment))
                .thenReturn(commentDto);

        ItemDto result = itemService.findById(2L, 5L);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getComments())
                .containsExactly(commentDto);

        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();

        verify(bookingRepository, never())
                .findFirstByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(
                        anyLong(),
                        any(),
                        any()
                );
    }

    @Test
    void findByIdShouldReturnBookingsForOwner() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setId(5L);
        item.setOwner(owner);

        ItemDto dto = new ItemDto();
        dto.setId(5L);

        Booking lastBooking = new Booking();
        lastBooking.setId(10L);

        Booking nextBooking = new Booking();
        nextBooking.setId(11L);

        BookingShortDto lastDto = new BookingShortDto();
        lastDto.setId(10L);

        BookingShortDto nextDto = new BookingShortDto();
        nextDto.setId(11L);

        when(itemRepository.findById(5L))
                .thenReturn(Optional.of(item));

        when(itemMapper.toItemDto(item))
                .thenReturn(dto);

        when(commentRepository.findAllByItemIdOrderByCreatedAsc(5L))
                .thenReturn(List.of());

        when(bookingRepository
                .findFirstByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(
                        eq(5L),
                        eq(BookingStatus.APPROVED),
                        any(LocalDateTime.class)
                ))
                .thenReturn(Optional.of(lastBooking));

        when(bookingRepository
                .findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                        eq(5L),
                        eq(BookingStatus.APPROVED),
                        any(LocalDateTime.class)
                ))
                .thenReturn(Optional.of(nextBooking));

        when(bookingMapper.toBookingShortDto(lastBooking))
                .thenReturn(lastDto);

        when(bookingMapper.toBookingShortDto(nextBooking))
                .thenReturn(nextDto);

        ItemDto result = itemService.findById(1L, 5L);

        assertThat(result.getLastBooking()).isEqualTo(lastDto);
        assertThat(result.getNextBooking()).isEqualTo(nextDto);
    }

    @Test
    void findAllByOwnerShouldReturnEmptyList() {
        User owner = new User();
        owner.setId(1L);

        when(userService.findById(1L))
                .thenReturn(owner);

        when(itemRepository.findAllByOwnerId(1L))
                .thenReturn(List.of());

        assertThat(itemService.findAllByOwner(1L))
                .isEmpty();

        verifyNoInteractions(commentRepository);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void findAllByOwnerShouldReturnItemsWithBookingsAndComments() {
        User owner = new User();
        owner.setId(1L);

        User author = new User();
        author.setId(2L);

        Item item = new Item();
        item.setId(5L);
        item.setOwner(owner);

        Comment comment = new Comment();
        comment.setId(20L);
        comment.setItem(item);
        comment.setAuthor(author);

        Booking lastBooking = new Booking();
        lastBooking.setId(30L);
        lastBooking.setItem(item);
        lastBooking.setStart(
                LocalDateTime.now().minusHours(2)
        );

        Booking nextBooking = new Booking();
        nextBooking.setId(31L);
        nextBooking.setItem(item);
        nextBooking.setStart(
                LocalDateTime.now().plusHours(2)
        );

        ItemDto itemDto = new ItemDto();
        itemDto.setId(5L);

        CommentDto commentDto = new CommentDto();
        commentDto.setId(20L);

        BookingShortDto lastDto = new BookingShortDto();
        lastDto.setId(30L);

        BookingShortDto nextDto = new BookingShortDto();
        nextDto.setId(31L);

        when(userService.findById(1L))
                .thenReturn(owner);

        when(itemRepository.findAllByOwnerId(1L))
                .thenReturn(List.of(item));

        when(commentRepository
                .findAllByItemIdInOrderByCreatedAsc(
                        List.of(5L)
                ))
                .thenReturn(List.of(comment));

        when(bookingRepository
                .findByOwnerAndStatus(
                        1L,
                        BookingStatus.APPROVED
                ))
                .thenReturn(
                        List.of(
                                lastBooking,
                                nextBooking
                        )
                );

        when(itemMapper.toItemDto(item))
                .thenReturn(itemDto);

        when(commentMapper.toCommentDto(comment))
                .thenReturn(commentDto);

        when(bookingMapper.toBookingShortDto(lastBooking))
                .thenReturn(lastDto);

        when(bookingMapper.toBookingShortDto(nextBooking))
                .thenReturn(nextDto);

        var result =
                itemService.findAllByOwner(1L);

        assertThat(result).hasSize(1);

        ItemDto resultItem =
                result.iterator().next();

        assertThat(resultItem.getComments())
                .containsExactly(commentDto);

        assertThat(resultItem.getLastBooking())
                .isEqualTo(lastDto);

        assertThat(resultItem.getNextBooking())
                .isEqualTo(nextDto);
    }

    @Test
    void updateShouldUpdateAllFields() {
        User owner = new User();
        owner.setId(1L);

        Item existing = new Item();
        existing.setId(5L);
        existing.setName("Старое имя");
        existing.setDescription("Старое описание");
        existing.setAvailable(false);
        existing.setOwner(owner);

        ItemDto input = new ItemDto();
        input.setName("Новое имя");
        input.setDescription("Новое описание");
        input.setAvailable(true);

        Item update = new Item();
        update.setName("Новое имя");
        update.setDescription("Новое описание");
        update.setAvailable(true);

        ItemDto resultDto = new ItemDto();
        resultDto.setId(5L);
        resultDto.setName("Новое имя");
        resultDto.setDescription("Новое описание");
        resultDto.setAvailable(true);

        when(itemRepository.findById(5L))
                .thenReturn(Optional.of(existing));

        when(itemMapper.toItem(input))
                .thenReturn(update);

        when(itemRepository.save(existing))
                .thenReturn(existing);

        when(itemMapper.toItemDto(existing))
                .thenReturn(resultDto);

        ItemDto result =
                itemService.update(1L, 5L, input);

        assertThat(result.getName())
                .isEqualTo("Новое имя");

        assertThat(existing.getName())
                .isEqualTo("Новое имя");

        assertThat(existing.getDescription())
                .isEqualTo("Новое описание");

        assertThat(existing.getAvailable())
                .isTrue();
    }

    @Test
    void updateShouldAllowPartialUpdate() {
        User owner = new User();
        owner.setId(1L);

        Item existing = new Item();
        existing.setId(5L);
        existing.setName("Старое имя");
        existing.setDescription("Описание");
        existing.setAvailable(true);
        existing.setOwner(owner);

        ItemDto input = new ItemDto();
        input.setName("Новое имя");

        Item update = new Item();
        update.setName("Новое имя");

        when(itemRepository.findById(5L))
                .thenReturn(Optional.of(existing));

        when(itemMapper.toItem(input))
                .thenReturn(update);

        when(itemRepository.save(existing))
                .thenReturn(existing);

        when(itemMapper.toItemDto(existing))
                .thenReturn(input);

        itemService.update(1L, 5L, input);

        assertThat(existing.getName())
                .isEqualTo("Новое имя");

        assertThat(existing.getDescription())
                .isEqualTo("Описание");

        assertThat(existing.getAvailable())
                .isTrue();
    }

    @Test
    void updateShouldThrowWhenUserIsNotOwner() {
        User owner = new User();
        owner.setId(2L);

        Item existing = new Item();
        existing.setId(5L);
        existing.setOwner(owner);

        ItemDto input = new ItemDto();
        input.setName("Новое имя");

        Item update = new Item();
        update.setName("Новое имя");

        when(itemRepository.findById(5L))
                .thenReturn(Optional.of(existing));

        when(itemMapper.toItem(input))
                .thenReturn(update);

        assertThatThrownBy(
                () -> itemService.update(
                        1L,
                        5L,
                        input
                )
        )
                .isInstanceOf(ForbiddenException.class);

        verify(itemRepository, never())
                .save(any());
    }

    @Test
    void addCommentShouldSaveCommentAfterCompletedBooking() {
        User author = new User();
        author.setId(2L);
        author.setName("Ivan");

        Item item = new Item();
        item.setId(5L);

        CommentDto input = new CommentDto();
        input.setText("Хорошая вещь");

        CommentDto resultDto = new CommentDto();
        resultDto.setId(10L);
        resultDto.setText("Хорошая вещь");

        when(userService.findById(2L))
                .thenReturn(author);

        when(itemRepository.findById(5L))
                .thenReturn(Optional.of(item));

        when(bookingRepository
                .existsByBookerIdAndItemIdAndStatusAndEndBefore(
                        eq(2L),
                        eq(5L),
                        eq(BookingStatus.APPROVED),
                        any(LocalDateTime.class)
                ))
                .thenReturn(true);

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> {
                    Comment comment =
                            invocation.getArgument(0);
                    comment.setId(10L);
                    return comment;
                });

        when(commentMapper.toCommentDto(any(Comment.class)))
                .thenReturn(resultDto);

        CommentDto result =
                itemService.addComment(
                        2L,
                        5L,
                        input
                );

        assertThat(result.getId()).isEqualTo(10L);

        ArgumentCaptor<Comment> captor =
                ArgumentCaptor.forClass(Comment.class);

        verify(commentRepository)
                .save(captor.capture());

        assertThat(captor.getValue().getAuthor())
                .isEqualTo(author);

        assertThat(captor.getValue().getItem())
                .isEqualTo(item);

        assertThat(captor.getValue().getCreated())
                .isNotNull();
    }

    @Test
    void addCommentShouldThrowWithoutCompletedBooking() {
        User user = new User();
        user.setId(2L);

        Item item = new Item();
        item.setId(5L);

        CommentDto input = new CommentDto();
        input.setText("Комментарий");

        when(userService.findById(2L))
                .thenReturn(user);

        when(itemRepository.findById(5L))
                .thenReturn(Optional.of(item));

        when(bookingRepository
                .existsByBookerIdAndItemIdAndStatusAndEndBefore(
                        eq(2L),
                        eq(5L),
                        eq(BookingStatus.APPROVED),
                        any(LocalDateTime.class)
                ))
                .thenReturn(false);

        assertThatThrownBy(
                () -> itemService.addComment(
                        2L,
                        5L,
                        input
                )
        )
                .isInstanceOf(ValidationException.class);

        verify(commentRepository, never())
                .save(any());
    }

    @Test
    void findByIdShouldThrowWhenItemDoesNotExist() {
        when(itemRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> itemService.findById(
                        1L,
                        999L
                )
        )
                .isInstanceOf(NotFoundException.class);
    }
}