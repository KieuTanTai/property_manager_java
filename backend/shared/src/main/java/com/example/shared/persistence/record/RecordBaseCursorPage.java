package com.example.shared.persistence.record;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public record RecordBaseCursorPage<T>(List<T> items, @Nullable UUID nextCursor, boolean hasMore) {
}
