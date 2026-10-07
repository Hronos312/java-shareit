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

    @Test
    void findByIdShouldReturnSavedUser() {
        User user = new User();
        user.setName("Ivan");
        user.setEmail("find@mail.ru");

        user = userRepository.save(user);

        User result =
                userService.findById(user.getId());

        assertThat(result.getId())
                .isEqualTo(user.getId());
    }

    @Test
    void findByIdShouldThrowForUnknownUser() {
        assertThatThrownBy(
                () -> userService.findById(99999L)
        )
                .isInstanceOf(
                        ru.practicum.shareit.exception.NotFoundException.class
                );
    }

    @Test
    void findAllShouldReturnUsers() {
        User first = new User();
        first.setName("Ivan");
        first.setEmail("all1@mail.ru");

        User second = new User();
        second.setName("Petr");
        second.setEmail("all2@mail.ru");

        userRepository.save(first);
        userRepository.save(second);

        assertThat(userService.findAll())
                .extracting(User::getEmail)
                .contains(
                        "all1@mail.ru",
                        "all2@mail.ru"
                );
    }

    @Test
    void findByEmailShouldReturnUser() {
        User user = new User();
        user.setName("Ivan");
        user.setEmail("email@mail.ru");

        userRepository.save(user);

        assertThat(
                userService.findByEmail("email@mail.ru")
        )
                .isPresent();
    }

    @Test
    void deleteShouldRemoveUser() {
        User user = new User();
        user.setName("Ivan");
        user.setEmail("delete@mail.ru");

        user = userRepository.save(user);

        Long id = user.getId();

        userService.delete(id);

        assertThat(userRepository.findById(id))
                .isEmpty();
    }

    @Test
    void deleteShouldThrowForUnknownUser() {
        assertThatThrownBy(
                () -> userService.delete(99999L)
        )
                .isInstanceOf(
                        ru.practicum.shareit.exception.NotFoundException.class
                );
    }

    @Test
    void updateShouldChangeEmail() {
        User user = new User();
        user.setName("Ivan");
        user.setEmail("old@mail.ru");

        user = userRepository.save(user);

        User update = new User();
        update.setEmail("new@mail.ru");

        User result =
                userService.update(
                        user.getId(),
                        update
                );

        assertThat(result.getName())
                .isEqualTo("Ivan");

        assertThat(result.getEmail())
                .isEqualTo("new@mail.ru");
    }

    @Test
    void updateShouldRejectEmailOfAnotherUser() {
        User first = new User();
        first.setName("Ivan");
        first.setEmail("first@mail.ru");

        User second = new User();
        second.setName("Petr");
        second.setEmail("second@mail.ru");

        first = userRepository.save(first);
        second = userRepository.save(second);

        User update = new User();
        update.setEmail(second.getEmail());

        Long firstId = first.getId();

        assertThatThrownBy(
                () -> userService.update(
                        firstId,
                        update
                )
        )
                .isInstanceOf(DuplicatedDataException.class);
    }

    @Test
    void updateShouldAllowSameEmailForSameUser() {
        User user = new User();
        user.setName("Ivan");
        user.setEmail("same@mail.ru");

        user = userRepository.save(user);

        User update = new User();
        update.setEmail("same@mail.ru");

        User result =
                userService.update(
                        user.getId(),
                        update
                );

        assertThat(result.getEmail())
                .isEqualTo("same@mail.ru");
    }
}