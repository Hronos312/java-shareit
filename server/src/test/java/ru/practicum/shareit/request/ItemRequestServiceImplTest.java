package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ItemRequestServiceImplTest {

    private ItemRequestRepository requestRepository;
    private ItemRepository itemRepository;
    private UserService userService;

    private ItemRequestServiceImpl requestService;

    @BeforeEach
    void setUp() {
        requestRepository = mock(ItemRequestRepository.class);
        itemRepository = mock(ItemRepository.class);
        userService = mock(UserService.class);

        requestService = new ItemRequestServiceImpl(
                requestRepository,
                itemRepository,
                new ItemRequestMapper(),
                userService
        );
    }

    @Test
    void createShouldSaveRequest() {
        User requester = new User();
        requester.setId(1L);
        requester.setName("Ivan");
        requester.setEmail("ivan@mail.ru");

        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");

        when(userService.findById(1L))
                .thenReturn(requester);

        when(requestRepository.save(any(ItemRequest.class)))
                .thenAnswer(invocation -> {
                    ItemRequest request =
                            invocation.getArgument(0);

                    request.setId(10L);

                    return request;
                });

        ItemRequestDto result =
                requestService.create(1L, requestDto);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getDescription())
                .isEqualTo("Нужна дрель");
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getItems()).isEmpty();

        ArgumentCaptor<ItemRequest> captor =
                ArgumentCaptor.forClass(ItemRequest.class);

        verify(requestRepository).save(captor.capture());

        ItemRequest savedRequest = captor.getValue();

        assertThat(savedRequest.getRequester())
                .isEqualTo(requester);
        assertThat(savedRequest.getDescription())
                .isEqualTo("Нужна дрель");
        assertThat(savedRequest.getCreated()).isNotNull();
    }

    @Test
    void findAllByRequesterShouldReturnRequestsWithItems() {
        User requester = new User();
        requester.setId(1L);

        ItemRequest request = new ItemRequest();
        request.setId(10L);
        request.setDescription("Нужна дрель");
        request.setRequester(requester);

        User owner = new User();
        owner.setId(2L);

        Item item = new Item();
        item.setId(20L);
        item.setName("Дрель Bosch");
        item.setOwner(owner);
        item.setRequest(request);

        when(userService.findById(1L))
                .thenReturn(requester);

        when(requestRepository
                .findAllByRequesterIdOrderByCreatedDesc(1L))
                .thenReturn(List.of(request));

        when(itemRepository.findAllByRequestIdIn(
                List.of(10L)
        )).thenReturn(List.of(item));

        Collection<ItemRequestDto> result =
                requestService.findAllByRequester(1L);

        assertThat(result).hasSize(1);

        ItemRequestDto resultRequest =
                result.iterator().next();

        assertThat(resultRequest.getId())
                .isEqualTo(10L);

        assertThat(resultRequest.getItems())
                .hasSize(1);

        assertThat(resultRequest.getItems().get(0).getId())
                .isEqualTo(20L);

        assertThat(resultRequest.getItems().get(0).getName())
                .isEqualTo("Дрель Bosch");

        assertThat(resultRequest.getItems().get(0).getOwnerId())
                .isEqualTo(2L);

        verify(itemRepository, times(1))
                .findAllByRequestIdIn(any());
    }

    @Test
    void findAllByOthersShouldUsePagination() {
        User user = new User();
        user.setId(1L);

        when(userService.findById(1L))
                .thenReturn(user);

        when(requestRepository
                .findAllByRequesterIdNotOrderByCreatedDesc(
                        eq(1L),
                        any(Pageable.class)
                ))
                .thenReturn(List.of());

        requestService.findAllByOthers(
                1L,
                5,
                10
        );

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(requestRepository)
                .findAllByRequesterIdNotOrderByCreatedDesc(
                        eq(1L),
                        captor.capture()
                );

        Pageable pageable = captor.getValue();

        assertThat(pageable.getOffset())
                .isEqualTo(5);

        assertThat(pageable.getPageSize())
                .isEqualTo(10);
    }

    @Test
    void findByIdShouldReturnRequestWithItems() {
        User user = new User();
        user.setId(1L);

        ItemRequest request = new ItemRequest();
        request.setId(10L);
        request.setDescription("Нужен шуруповёрт");

        User owner = new User();
        owner.setId(2L);

        Item item = new Item();
        item.setId(20L);
        item.setName("Шуруповёрт");
        item.setOwner(owner);
        item.setRequest(request);

        when(userService.findById(1L))
                .thenReturn(user);

        when(requestRepository.findById(10L))
                .thenReturn(Optional.of(request));

        when(itemRepository.findAllByRequestIdIn(
                List.of(10L)
        )).thenReturn(List.of(item));

        ItemRequestDto result =
                requestService.findById(
                        1L,
                        10L
                );

        assertThat(result.getId())
                .isEqualTo(10L);

        assertThat(result.getDescription())
                .isEqualTo("Нужен шуруповёрт");

        assertThat(result.getItems())
                .hasSize(1);

        assertThat(result.getItems().get(0).getId())
                .isEqualTo(20L);
    }

    @Test
    void findByIdShouldThrowWhenRequestDoesNotExist() {
        User user = new User();
        user.setId(1L);

        when(userService.findById(1L))
                .thenReturn(user);

        when(requestRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> requestService.findById(
                        1L,
                        999L
                )
        )
                .isInstanceOf(NotFoundException.class);

        verify(itemRepository, never())
                .findAllByRequestIdIn(any());
    }
}