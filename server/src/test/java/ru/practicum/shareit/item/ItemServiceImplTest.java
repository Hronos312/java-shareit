package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

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
}