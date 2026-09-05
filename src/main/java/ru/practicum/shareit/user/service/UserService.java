package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User create(User user) {
        validateEmail(user.getEmail());

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new DuplicatedDataException("Пользователь с таким email уже существует");
        }

        user.setId(null);

        return userRepository.save(user);
    }

    public  User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    public Collection<User> findAll() {
        return userRepository.findAll();
    }

    public void delete(Long id) {
        if (userRepository.findById(id).isPresent()) {
            userRepository.deleteById(id);
        } else {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User update(Long id, User updatedUser) {
        User user = findById(id);

        if (updatedUser.getName() != null && updatedUser.getName().isBlank()) {
            throw new ValidationException("Имя пользователя не может быть пустым");
        }

        if (updatedUser.getEmail() != null) {
            validateEmail(updatedUser.getEmail());

            Optional<User> userWithSameEmail = userRepository.findByEmail(updatedUser.getEmail());

            if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(id)) {
                throw new DuplicatedDataException("Пользователь с таким email уже существует");
            }
        }

        if (updatedUser.getName() != null) {
            user.setName(updatedUser.getName());
        }

        if (updatedUser.getEmail() != null) {
            user.setEmail(updatedUser.getEmail());
        }

        return userRepository.save(user);
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ValidationException("Некорректный email");
        }
    }

}
