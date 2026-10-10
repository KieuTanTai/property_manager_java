package com.example.shared.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import com.example.shared.persistence.record.RecordBaseCursorPage;

public final class SharedGetApplyPagingRepository {
    private SharedGetApplyPagingRepository() {
    }

    public static void validatePageSize(int pageSize) {
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be greater than zero.");
        }
    }

    public static <T> RecordBaseCursorPage<T> applyPaging(
            Iterable<T> source, int pageSize, Function<T, UUID> cursorFunction) {
        validatePageSize(pageSize);
        List<T> items = new ArrayList<>();
        for (T value : source) {
            items.add(value);
            if (items.size() == pageSize + 1) {
                break;
            }
        }
        boolean hasMore = items.size() > pageSize;
        if (hasMore) {
            items.removeLast();
        }
        UUID nextCursor = hasMore && !items.isEmpty() ? cursorFunction.apply(items.getLast()) : null;
        return new RecordBaseCursorPage<>(items, nextCursor, hasMore);
    }
}
