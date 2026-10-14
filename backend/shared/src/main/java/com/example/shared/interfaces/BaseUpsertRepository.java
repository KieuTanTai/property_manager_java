package com.example.shared.interfaces;

import java.util.List;

public interface BaseUpsertRepository<T> {
    T add(T entity);
    List<T> addAll(Iterable<T> entities);
    T update(T entity);
    List<T> updateAll(Iterable<T> entities);
}
