package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void searchShouldFindOnlyAvailableItemsByNameOrDescription() {
        User owner = new User();
        owner.setName("Ivan");
        owner.setEmail("ivan-search@mail.ru");

        owner = userRepository.save(owner);

        Item byName = new Item();
        byName.setName("Дрель");
        byName.setDescription("Инструмент");
        byName.setAvailable(true);
        byName.setOwner(owner);

        Item byDescription = new Item();
        byDescription.setName("Инструмент");
        byDescription.setDescription("Мощная электрическая дрель");
        byDescription.setAvailable(true);
        byDescription.setOwner(owner);

        Item unavailable = new Item();
        unavailable.setName("Дрель");
        unavailable.setDescription("Недоступная вещь");
        unavailable.setAvailable(false);
        unavailable.setOwner(owner);

        Item unrelated = new Item();
        unrelated.setName("Молоток");
        unrelated.setDescription("Для гвоздей");
        unrelated.setAvailable(true);
        unrelated.setOwner(owner);

        itemRepository.save(byName);
        itemRepository.save(byDescription);
        itemRepository.save(unavailable);
        itemRepository.save(unrelated);

        Collection<ItemDto> result = itemService.search("дрель");

        assertThat(result)
                .hasSize(2)
                .extracting(ItemDto::getName)
                .containsExactlyInAnyOrder(
                        "Дрель",
                        "Инструмент"
                );

        assertThat(result)
                .allMatch(ItemDto::getAvailable);
    }

    @Test
    void searchShouldBeCaseInsensitive() {
        User owner = new User();
        owner.setName("Petr");
        owner.setEmail("petr-search@mail.ru");

        owner = userRepository.save(owner);

        Item item = new Item();
        item.setName("ЭЛЕКТРИЧЕСКАЯ ДРЕЛЬ");
        item.setDescription("Инструмент");
        item.setAvailable(true);
        item.setOwner(owner);

        itemRepository.save(item);

        Collection<ItemDto> result =
                itemService.search("дрель");

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getName())
                .isEqualTo("ЭЛЕКТРИЧЕСКАЯ ДРЕЛЬ");
    }

    @Test
    void searchShouldReturnEmptyForBlankText() {
        Collection<ItemDto> result =
                itemService.search("   ");

        assertThat(result).isEmpty();
    }
}