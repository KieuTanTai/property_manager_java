package com.example.shared.interfaces;

import java.util.List;
import java.util.Optional;

public interface BaseAssociativeRepository<T, ID> {
    List<T> findAll();
    List<T> findByFirstId(ID id);
    List<T> findBySecondId(ID id);
    Optional<T> findByIds(ID firstId, ID secondId);
    boolean existsByIds(ID firstId, ID secondId);
    T add(T entity);
    List<T> addAll(Iterable<T> entities);
    void deleteByFirstId(ID id);
    void deleteBySecondId(ID id);
    void deleteByIds(ID firstId, ID secondId);
}
