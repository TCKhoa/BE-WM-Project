package org.wp.wpproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.wp.wpproject.dto.ProductDTO;
import org.wp.wpproject.entity.Product;
import org.wp.wpproject.repository.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
    private static final int SHORT_ID_LENGTH = 6; // độ dài ID ngắn

    @Autowired private ProductRepository productRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UnitRepository unitRepository;
    @Autowired private LocationRepository locationRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .filter(p -> p.getDeletedAt() == null) // chỉ lấy sp chưa xóa
                .toList();
    }

    public Optional<Product> getProductById(String id) {
        return productRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null);
    }

    public Product createProduct(ProductDTO dto, MultipartFile file) {
        Product product = new Product();
        mapDtoToEntity(dto, product);

        // --- Sinh ID ngắn và kiểm tra không trùng ---
        String id;
        do {
            id = generateShortId(SHORT_ID_LENGTH);
        } while (productRepository.existsById(id));
        product.setId(id);

        if (file != null && !file.isEmpty()) {
            String fileUrl = saveFile(file);
            product.setImageUrl(fileUrl);
        }

        return productRepository.save(product);
    }

    public Optional<Product> updateProduct(String id, ProductDTO dto, MultipartFile file) {
        return productRepository.findById(id).map(product -> {
            mapDtoToEntity(dto, product);

            if (file != null && !file.isEmpty()) {
                deleteFile(product.getImageUrl());
                String fileUrl = saveFile(file);
                product.setImageUrl(fileUrl);
            }

            product.setUpdatedAt(LocalDateTime.now());
            return productRepository.save(product);
        });
    }

    public boolean hideProduct(String id) {
        return productRepository.findById(id).map(product -> {
            product.setDeletedAt(LocalDateTime.now());
            productRepository.save(product);
            return true;
        }).orElse(false);
    }

    private void mapDtoToEntity(ProductDTO dto, Product product) {
        product.setProductCode(dto.getProductCode());
        product.setName(dto.getName());
        product.setImportPrice(dto.getImportPrice());
        product.setStock(dto.getStock());
        product.setDescription(dto.getDescription());

        if (dto.getBrandId() != null) {
            product.setBrand(brandRepository.findById(dto.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Brand not found")));
        }
        if (dto.getCategoryId() != null) {
            product.setCategory(categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found")));
        }
        if (dto.getUnitId() != null) {
            product.setUnit(unitRepository.findById(dto.getUnitId())
                    .orElseThrow(() -> new RuntimeException("Unit not found")));
        }
        if (dto.getLocationId() != null) {
            product.setLocation(locationRepository.findById(dto.getLocationId())
                    .orElseThrow(() -> new RuntimeException("Location not found")));
        }
    }

    private String saveFile(MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            if (originalName == null) throw new RuntimeException("File không hợp lệ");

            String extension = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
            if (!(extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png") || extension.equals("webp"))) {
                throw new RuntimeException("Chỉ hỗ trợ JPG, JPEG, PNG, WEBP");
            }

            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            String uniqueFileName = UUID.randomUUID() + "_" + originalName;
            Path path = Paths.get(UPLOAD_DIR + uniqueFileName);
            Files.write(path, file.getBytes());

            return "/uploads/" + uniqueFileName;
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file", e);
        }
    }

    private void deleteFile(String fileUrl) {
        if (fileUrl != null && fileUrl.startsWith("/uploads/")) {
            String filePath = UPLOAD_DIR + fileUrl.replace("/uploads/", "");
            File file = new File(filePath);
            if (file.exists()) file.delete();
        }
    }

    // --- Hàm sinh ID ngắn ---
    private static String generateShortId(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int idx = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(idx));
        }
        return sb.toString();
    }
}
