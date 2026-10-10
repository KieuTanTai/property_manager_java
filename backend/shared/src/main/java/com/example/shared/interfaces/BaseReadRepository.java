package com.example.shared.interfaces;

import java.util.List;
import java.util.Optional;

public interface BaseReadRepository<T, ID> {
    List<T> findAll();
    Optional<T> findById(ID id);
    List<T> findAllById(Iterable<ID> ids);
    Optional<T> findTrackedById(ID id);
    boolean existsById(ID id);
}
