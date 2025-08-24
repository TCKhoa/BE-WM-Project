package org.wp.wpproject.controller;

import org.wp.wpproject.entity.Location;
import org.wp.wpproject.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    // Lấy danh sách location chưa bị xóa
    @GetMapping
    public List<Location> getAllLocations() {
        return locationService.getAllActiveLocations();
    }

    // Lấy location theo id (chỉ khi chưa bị xóa)
    @GetMapping("/{id}")
    public ResponseEntity<Location> getLocationById(@PathVariable Integer id) {
        Optional<Location> locationOpt = locationService.getLocationById(id);
        return locationOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Tạo mới location
    @PostMapping
    public Location createLocation(@RequestBody Location location) {
        location.setCreatedAt(LocalDateTime.now());
        location.setUpdatedAt(LocalDateTime.now());
        location.setDeletedAt(null);
        return locationService.createLocation(location);
    }

    // Cập nhật location
    @PutMapping("/{id}")
    public ResponseEntity<Location> updateLocation(@PathVariable Integer id, @RequestBody Location locationDetails) {
        Optional<Location> updatedLocation = locationService.updateLocation(id, locationDetails);
        return updatedLocation.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Xóa mềm location (set deletedAt)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteLocation(@PathVariable Integer id) {
        boolean deleted = locationService.softDeleteLocation(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
