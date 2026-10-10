package com.example.shared.logging;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LogPool implements ILogPool {
    private final ConcurrentLinkedQueue<LogEntry> entries = new ConcurrentLinkedQueue<>();

    @Override
    public void add(String module, String layer, String file, String member,
            String level, String message, Throwable exception) {
        entries.add(new LogEntry(OffsetDateTime.now(ZoneOffset.UTC), module, file, layer, member,
                level, message, exception == null ? null : exception.getClass().getSimpleName()));
    }

    @Override
    public List<LogEntry> fetch() {
        return new ArrayList<>(entries);
    }

    public int size() {
        return entries.size();
    }
}
