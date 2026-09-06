package ru.ifmo.movies_app.service;

import java.util.Collection;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationSupport {

    private PaginationSupport() {
    }

    public static int normalizePage(int requestedPage) {
        return Math.max(requestedPage, 0);
    }

    public static int normalizeSize(int requestedSize, int defaultSize, int maxSize) {
        if (defaultSize <= 0) {
            throw new IllegalArgumentException("Default page size must be positive");
        }
        if (maxSize < defaultSize) {
            throw new IllegalArgumentException("Max page size must be greater than or equal to default page size");
        }
        if (requestedSize <= 0) {
            return defaultSize;
        }
        return Math.min(requestedSize, maxSize);
    }

    public static Pageable createPageable(int page,
                                          int size,
                                          String sort,
                                          String direction,
                                          Collection<String> allowedSorts,
                                          int defaultSize,
                                          int maxSize) {
        int safePage = normalizePage(page);
        int safeSize = normalizeSize(size, defaultSize, maxSize);
        if (sort == null || sort.isBlank() || !allowedSorts.contains(sort)) {
            return PageRequest.of(safePage, safeSize);
        }
        Sort.Direction safeDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return PageRequest.of(safePage, safeSize, Sort.by(safeDirection, sort));
    }
}
