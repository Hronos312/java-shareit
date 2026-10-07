package ru.practicum.shareit.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
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

    @Test
    void shouldNavigateBetweenPages() {
        Sort sort = Sort.by("start");

        OffsetPageRequest pageable =
                new OffsetPageRequest(10, 5, sort);

        assertThat(pageable.getSort()).isEqualTo(sort);
        assertThat(pageable.hasPrevious()).isTrue();

        Pageable next = pageable.next();

        assertThat(next.getOffset()).isEqualTo(15);
        assertThat(next.getPageSize()).isEqualTo(5);

        Pageable previous =
                pageable.previousOrFirst();

        assertThat(previous.getOffset()).isEqualTo(5);

        Pageable first = pageable.first();

        assertThat(first.getOffset()).isZero();

        Pageable thirdPage =
                pageable.withPage(3);

        assertThat(thirdPage.getOffset()).isEqualTo(15);
    }

    @Test
    void previousOrFirstShouldReturnFirstWhenNoPreviousPage() {
        OffsetPageRequest pageable =
                new OffsetPageRequest(
                        0,
                        10,
                        Sort.unsorted()
                );

        assertThat(pageable.hasPrevious()).isFalse();

        Pageable result =
                pageable.previousOrFirst();

        assertThat(result.getOffset()).isZero();
        assertThat(result.getPageSize()).isEqualTo(10);
    }


}