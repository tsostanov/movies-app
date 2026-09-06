package ru.ifmo.movies_app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PaginationSupportTest {

    @ParameterizedTest
    @CsvSource({
            "-10, 0",
            "0, 0",
            "3, 3"
    })
    void normalizePageClampsNegativeValues(int requested, int expected) {
        assertThat(PaginationSupport.normalizePage(requested)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "-1, 10",
            "0, 10",
            "1, 1",
            "50, 50",
            "1000, 50"
    })
    void normalizeSizeUsesDefaultAndMaxBounds(int requested, int expected) {
        assertThat(PaginationSupport.normalizeSize(requested, 10, 50)).isEqualTo(expected);
    }

    @Test
    void normalizeSizeRejectsInvalidConfiguration() {
        assertThatThrownBy(() -> PaginationSupport.normalizeSize(10, 0, 50))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PaginationSupport.normalizeSize(10, 20, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createPageableAppliesAllowListedSortAndDirection() {
        Pageable pageable = PaginationSupport.createPageable(
                2,
                25,
                "name",
                "DESC",
                List.of("name", "genre"),
                10,
                100);

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(25);
        Sort.Order order = pageable.getSort().getOrderFor("name");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void createPageableIgnoresUnknownSortField() {
        Pageable pageable = PaginationSupport.createPageable(
                -1,
                1000,
                "id",
                "desc",
                List.of("name", "genre"),
                10,
                100);

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(100);
        assertThat(pageable.getSort().isUnsorted()).isTrue();
    }
}
