package org.wp.wpproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wp.wpproject.entity.Brand;
import org.wp.wpproject.repository.BrandRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BrandService {

    @Autowired
    private BrandRepository brandRepository;

    // Lấy danh sách brand chưa bị xóa (deletedAt = null)
    public List<Brand> getAllActiveBrands() {
        return brandRepository.findByDeletedAtIsNull();
    }

    // Lấy brand theo id (chỉ khi chưa bị xóa)
    public Optional<Brand> getBrandById(Integer id) {
        return brandRepository.findByIdAndDeletedAtIsNull(id);
    }

    // Tạo mới brand
    public Brand createBrand(Brand brand) {
        return brandRepository.save(brand);
    }

    // Cập nhật brand
    public Optional<Brand> updateBrand(Integer id, Brand brandDetails) {
        Optional<Brand> brandOpt = brandRepository.findByIdAndDeletedAtIsNull(id);
        if (brandOpt.isEmpty()) {
            return Optional.empty();
        }
        Brand brand = brandOpt.get();
        brand.setName(brandDetails.getName());
        brand.setSlug(brandDetails.getSlug());
        brand.setUpdatedAt(LocalDateTime.now());
        Brand updatedBrand = brandRepository.save(brand);
        return Optional.of(updatedBrand);
    }

    // Xóa mềm brand (set deletedAt)
    public boolean softDeleteBrand(Integer id) {
        Optional<Brand> brandOpt = brandRepository.findByIdAndDeletedAtIsNull(id);
        if (brandOpt.isEmpty()) {
            return false;
        }
        Brand brand = brandOpt.get();
        brand.setDeletedAt(LocalDateTime.now());
        brandRepository.save(brand);
        return true;
    }
}
