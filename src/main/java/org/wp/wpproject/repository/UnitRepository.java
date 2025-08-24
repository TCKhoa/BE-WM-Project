package org.wp.wpproject.repository;

import org.wp.wpproject.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UnitRepository extends JpaRepository<Unit, Integer> {
    List<Unit> findByDeletedAtIsNull();

    Optional<Unit> findByIdAndDeletedAtIsNull(Integer id);
}
