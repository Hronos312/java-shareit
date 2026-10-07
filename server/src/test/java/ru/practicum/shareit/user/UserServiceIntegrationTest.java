package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createShouldPersistUser() {
        User user = new User();
        user.setName("Ivan");
        user.setEmail("ivan-integration@mail.ru");

        User created = userService.create(user);

        assertThat(created.getId()).isNotNull();

        User saved = userRepository.findById(created.getId())
                .orElseThrow();

        assertThat(saved.getName())
                .isEqualTo("Ivan");

        assertThat(saved.getEmail())
                .isEqualTo("ivan-integration@mail.ru");
    }

    @Test
    void createShouldRejectDuplicateEmail() {
        User first = new User();
        first.setName("Ivan");
        first.setEmail("duplicate@mail.ru");

        userService.create(first);

        User second = new User();
        second.setName("Petr");
        second.setEmail("duplicate@mail.ru");

        assertThatThrownBy(
                () -> userService.create(second)
        )
                .isInstanceOf(DuplicatedDataException.class);
    }

    @Test
    void updateShouldPersistPartialChanges() {
        User user = new User();
        user.setName("Old name");
        user.setEmail("update-integration@mail.ru");

        User created = userService.create(user);

        User update = new User();
        update.setName("New name");

        User updated =
                userService.update(
                        created.getId(),
                        update
                );

        assertThat(updated.getName())
                .isEqualTo("New name");

        assertThat(updated.getEmail())
                .isEqualTo("update-integration@mail.ru");

        User saved = userRepository
                .findById(created.getId())
                .orElseThrow();

        assertThat(saved.getName())
                .isEqualTo("New name");

        assertThat(saved.getEmail())
                .isEqualTo("update-integration@mail.ru");
    }
}