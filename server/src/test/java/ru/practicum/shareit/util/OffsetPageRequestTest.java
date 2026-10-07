package ru.practicum.shareit.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OffsetPageRequestTest {

    @Test
    void shouldCreateWithValidArguments() {
        OffsetPageRequest pageable =
                new OffsetPageRequest(
                        5,
                        10,
                        Sort.unsorted()
                );

        assertThat(pageable.getOffset()).isEqualTo(5);
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getPageNumber()).isZero();
    }

    @Test
    void shouldRejectNegativeOffset() {
        assertThatThrownBy(
                () -> new OffsetPageRequest(
                        -1,
                        10,
                        Sort.unsorted()
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Offset не может быть отрицательным");
    }

    @Test
    void shouldRejectZeroPageSize() {
        assertThatThrownBy(
                () -> new OffsetPageRequest(
                        0,
                        0,
                        Sort.unsorted()
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Page size должен быть больше нуля");
    }

    @Test
    void shouldRejectNegativePageSize() {
        assertThatThrownBy(
                () -> new OffsetPageRequest(
                        0,
                        -5,
                        Sort.unsorted()
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }
}