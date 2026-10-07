package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private ItemRequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findAllByOthersShouldExcludeOwnRequestsAndSortByCreatedDesc() {
        User currentUser = createUser(
                "Ivan",
                "ivan-requests@mail.ru"
        );

        User otherUser = createUser(
                "Petr",
                "petr-requests@mail.ru"
        );

        createRequest(
                currentUser,
                "Мой запрос",
                LocalDateTime.now().minusHours(1)
        );

        createRequest(
                otherUser,
                "Старый чужой запрос",
                LocalDateTime.now().minusHours(3)
        );

        createRequest(
                otherUser,
                "Новый чужой запрос",
                LocalDateTime.now().minusMinutes(30)
        );

        Collection<ItemRequestDto> result =
                requestService.findAllByOthers(
                        currentUser.getId(),
                        0,
                        10
                );

        assertThat(result)
                .hasSize(2)
                .extracting(ItemRequestDto::getDescription)
                .containsExactly(
                        "Новый чужой запрос",
                        "Старый чужой запрос"
                );
    }

    @Test
    void findAllByOthersShouldApplyOffsetAndSize() {
        User currentUser = createUser(
                "Ivan",
                "ivan-pagination@mail.ru"
        );

        User otherUser = createUser(
                "Petr",
                "petr-pagination@mail.ru"
        );

        createRequest(
                otherUser,
                "Самый новый",
                LocalDateTime.now().minusMinutes(10)
        );

        createRequest(
                otherUser,
                "Средний",
                LocalDateTime.now().minusMinutes(20)
        );

        createRequest(
                otherUser,
                "Самый старый",
                LocalDateTime.now().minusMinutes(30)
        );

        Collection<ItemRequestDto> result =
                requestService.findAllByOthers(
                        currentUser.getId(),
                        1,
                        1
                );

        assertThat(result)
                .hasSize(1);

        assertThat(result.iterator().next().getDescription())
                .isEqualTo("Средний");
    }

    @Test
    void findAllByOthersShouldIncludeRequestsFromDifferentUsers() {
        User currentUser = createUser(
                "Ivan",
                "ivan-different@mail.ru"
        );

        User firstOther = createUser(
                "Petr",
                "petr-different@mail.ru"
        );

        User secondOther = createUser(
                "Anna",
                "anna-different@mail.ru"
        );

        createRequest(
                firstOther,
                "Запрос Петра",
                LocalDateTime.now().minusHours(2)
        );

        createRequest(
                secondOther,
                "Запрос Анны",
                LocalDateTime.now().minusHours(1)
        );

        Collection<ItemRequestDto> result =
                requestService.findAllByOthers(
                        currentUser.getId(),
                        0,
                        10
                );

        assertThat(result)
                .hasSize(2)
                .extracting(ItemRequestDto::getDescription)
                .containsExactly(
                        "Запрос Анны",
                        "Запрос Петра"
                );
    }

    private User createUser(
            String name,
            String email) {

        User user = new User();
        user.setName(name);
        user.setEmail(email);

        return userRepository.save(user);
    }

    private ItemRequest createRequest(
            User requester,
            String description,
            LocalDateTime created) {

        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequester(requester);
        request.setCreated(created);

        return requestRepository.save(request);
    }
}