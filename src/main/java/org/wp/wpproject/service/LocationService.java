package org.wp.wpproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wp.wpproject.entity.Location;
import org.wp.wpproject.repository.LocationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    // Lấy danh sách location chưa bị xóa
    public List<Location> getAllActiveLocations() {
        return locationRepository.findByDeletedAtIsNull();
    }

    // Lấy location theo id mà chưa bị xóa
    public Optional<Location> getLocationById(Integer id) {
        return locationRepository.findByIdAndDeletedAtIsNull(id);
    }

    // Tạo mới location
    public Location createLocation(Location location) {
        return locationRepository.save(location);
    }

    // Cập nhật location
    public Optional<Location> updateLocation(Integer id, Location locationDetails) {
        Optional<Location> locationOpt = locationRepository.findByIdAndDeletedAtIsNull(id);
        if (locationOpt.isEmpty()) {
            return Optional.empty();
        }
        Location location = locationOpt.get();
        location.setName(locationDetails.getName());
        location.setCode(locationDetails.getCode());
        location.setUpdatedAt(LocalDateTime.now());
        Location updatedLocation = locationRepository.save(location);
        return Optional.of(updatedLocation);
    }

    // Xóa mềm location (set deletedAt)
    public boolean softDeleteLocation(Integer id) {
        Optional<Location> locationOpt = locationRepository.findByIdAndDeletedAtIsNull(id);
        if (locationOpt.isEmpty()) {
            return false;
        }
        Location location = locationOpt.get();
        location.setDeletedAt(LocalDateTime.now());
        locationRepository.save(location);
        return true;
    }
}
