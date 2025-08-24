package org.wp.wpproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wp.wpproject.entity.Unit;
import org.wp.wpproject.repository.UnitRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UnitService {

    @Autowired
    private UnitRepository unitRepository;

    public List<Unit> getAllActiveUnits() {
        return unitRepository.findByDeletedAtIsNull();
    }

    public Optional<Unit> getUnitById(Integer id) {
        return unitRepository.findByIdAndDeletedAtIsNull(id);
    }

    public Unit createUnit(Unit unit) {
        return unitRepository.save(unit);
    }

    public Optional<Unit> updateUnit(Integer id, Unit unitDetails) {
        Optional<Unit> unitOpt = unitRepository.findByIdAndDeletedAtIsNull(id);
        if (unitOpt.isEmpty()) {
            return Optional.empty();
        }
        Unit unit = unitOpt.get();
        unit.setName(unitDetails.getName());
        unit.setSlug(unitDetails.getSlug());
        unit.setUpdatedAt(LocalDateTime.now());
        Unit updatedUnit = unitRepository.save(unit);
        return Optional.of(updatedUnit);
    }

    public boolean softDeleteUnit(Integer id) {
        Optional<Unit> unitOpt = unitRepository.findByIdAndDeletedAtIsNull(id);
        if (unitOpt.isEmpty()) {
            return false;
        }
        Unit unit = unitOpt.get();
        unit.setDeletedAt(LocalDateTime.now());
        unitRepository.save(unit);
        return true;
    }
}
