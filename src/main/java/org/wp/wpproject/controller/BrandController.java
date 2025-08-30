package org.wp.wpproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wp.wpproject.entity.Brand;
import org.wp.wpproject.entity.User;
import org.wp.wpproject.service.BrandService;
import org.wp.wpproject.service.HistoryLogService;
import org.wp.wpproject.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/brands")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @Autowired
    private HistoryLogService historyLogService;

    @Autowired
    private UserService userService;

    // Lấy danh sách brand chưa bị xóa
    @GetMapping
    public List<Brand> getAllBrands() {
        return brandService.getAllActiveBrands();
    }

    // Lấy brand theo id
    @GetMapping("/{id}")
    public ResponseEntity<Brand> getBrandById(@PathVariable Integer id) {
        Optional<Brand> brandOpt = brandService.getBrandById(id);
        return brandOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Tạo mới brand
    @PostMapping
    public ResponseEntity<Brand> createBrand(@RequestBody Brand brand) {
        User currentUser = userService.getCurrentUser();

        brand.setCreatedAt(LocalDateTime.now());
        brand.setUpdatedAt(LocalDateTime.now());
        brand.setDeletedAt(null);

        Brand created = brandService.createBrand(brand);

        // Ghi vào history log
        historyLogService.logAction(
                "Tạo thương hiệu " + created.getName(),
                currentUser
        );

        return ResponseEntity.ok(created);
    }

    // Cập nhật brand
    @PutMapping("/{id}")
    public ResponseEntity<Brand> updateBrand(@PathVariable Integer id, @RequestBody Brand brandDetails) {
        User currentUser = userService.getCurrentUser();

        Optional<Brand> updatedBrand = brandService.updateBrand(id, brandDetails);

        updatedBrand.ifPresent(b -> historyLogService.logAction(
                "Cập nhật thương hiệu " + b.getName(),
                currentUser
        ));

        return updatedBrand.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Xóa mềm brand (ẩn bằng cách set deletedAt)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Integer id) {
        User currentUser = userService.getCurrentUser();

        Optional<Brand> brandOpt = brandService.getBrandById(id);
        boolean deleted = brandService.softDeleteBrand(id);

        if (deleted && brandOpt.isPresent()) {
            historyLogService.logAction(
                    "Xóa thương hiệu " + brandOpt.get().getName(),
                    currentUser
            );
        }

        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
