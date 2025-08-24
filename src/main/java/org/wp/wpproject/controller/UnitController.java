package org.wp.wpproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wp.wpproject.entity.Unit;
import org.wp.wpproject.service.UnitService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/units")
public class UnitController {

    @Autowired
    private UnitService unitService;

    // Lấy danh sách Unit chưa xóa
    @GetMapping
    public List<Unit> getAllUnits() {
        return unitService.getAllActiveUnits();
    }

    // Lấy Unit theo id (chưa xóa)
    @GetMapping("/{id}")
    public ResponseEntity<Unit> getUnitById(@PathVariable Integer id) {
        Optional<Unit> unitOpt = unitService.getUnitById(id);
        return unitOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Tạo mới Unit
    @PostMapping
    public Unit createUnit(@RequestBody Unit unit) {
        return unitService.createUnit(unit);
    }

    // Cập nhật Unit
    @PutMapping("/{id}")
    public ResponseEntity<Unit> updateUnit(@PathVariable Integer id, @RequestBody Unit unitDetails) {
        Optional<Unit> updatedUnit = unitService.updateUnit(id, unitDetails);
        return updatedUnit.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Xóa mềm (ẩn) Unit
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUnit(@PathVariable Integer id) {
        boolean deleted = unitService.softDeleteUnit(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
