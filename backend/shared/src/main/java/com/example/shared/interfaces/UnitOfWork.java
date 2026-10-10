package com.example.shared.interfaces;

public interface UnitOfWork extends AutoCloseable {
    int saveChanges();

    @Override
    default void close() {
    }
}
