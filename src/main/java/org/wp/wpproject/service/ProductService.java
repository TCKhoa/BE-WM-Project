package org.wp.wpproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.wp.wpproject.dto.ProductDTO;
import org.wp.wpproject.entity.Product;
import org.wp.wpproject.entity.User;
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

    private static final String UPLOAD_DIR = System.getProperty("user.dir")
            + File.separator + "uploads" + File.separator;
    private static final int SHORT_ID_LENGTH = 6;

    @Autowired private ProductRepository productRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UnitRepository unitRepository;
    @Autowired private LocationRepository locationRepository;
    @Autowired private HistoryLogService historyLogService;
    @Autowired private UserService userService;

    // --- Lấy tất cả sản phẩm chưa bị xóa ---
    public List<Product> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .filter(p -> p.getDeletedAt() == null)
                .toList();
    }

    // --- Lấy sản phẩm theo ID ---
    public Optional<Product> getProductById(String id) {
        return productRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null);
    }

    // --- Lấy sản phẩm theo location ---
    public List<Product> getProductsByLocationId(Integer locationId) {
        return productRepository.findByLocationIdAndDeletedAtIsNull(locationId);
    }

    // --- Tạo sản phẩm mới ---
    public Product createProduct(ProductDTO dto, MultipartFile file) {
        Product product = new Product();
        mapDtoToEntity(dto, product);

        // Sinh ID ngắn và đảm bảo không trùng
        String id;
        do {
            id = generateShortId(SHORT_ID_LENGTH);
        } while (productRepository.existsById(id));
        product.setId(id);

        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        if (file != null && !file.isEmpty()) {
            String fileUrl = saveFile(file);
            product.setImageUrl(fileUrl);
        }

        Product saved = productRepository.save(product);

        // --- Ghi log ---
        User currentUser = userService.getCurrentUser();
        historyLogService.logAction("Tạo sản phẩm: " + saved.getName(), currentUser, "SUCCESS");

        return saved;
    }

    // --- Cập nhật sản phẩm ---
    public Optional<Product> updateProduct(String id, ProductDTO dto, MultipartFile file) {
        return productRepository.findById(id).map(product -> {
            mapDtoToEntity(dto, product);

            if (file != null && !file.isEmpty()) {
                deleteFile(product.getImageUrl());
                String fileUrl = saveFile(file);
                product.setImageUrl(fileUrl);
            }

            product.setUpdatedAt(LocalDateTime.now());
            Product saved = productRepository.save(product);

            // --- Ghi log ---
            User currentUser = userService.getCurrentUser();
            historyLogService.logAction("Cập nhật sản phẩm: " + saved.getName(), currentUser, "SUCCESS");

            return saved;
        });
    }

    // --- Ẩn (xóa mềm) sản phẩm ---
    public boolean hideProduct(String id) {
        return productRepository.findById(id).map(product -> {
            product.setDeletedAt(LocalDateTime.now());
            productRepository.save(product);

            // --- Ghi log ---
            User currentUser = userService.getCurrentUser();
            historyLogService.logAction("Xóa (ẩn) sản phẩm: " + product.getName(), currentUser, "SUCCESS");

            return true;
        }).orElse(false);
    }

    /**
     * Xóa vĩnh viễn các sản phẩm đã bị xóa mềm lâu hơn X ngày
     *
     * @param daysThreshold số ngày giữ sản phẩm bị xóa mềm
     */
    public void permanentlyDeleteOldProducts(int daysThreshold) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysThreshold);

        List<Product> oldDeletedProducts = productRepository.findAll().stream()
                .filter(p -> p.getDeletedAt() != null && p.getDeletedAt().isBefore(cutoffDate))
                .toList();

        for (Product product : oldDeletedProducts) {
            // Xóa file ảnh nếu có
            deleteFile(product.getImageUrl());

            // Xóa luôn khỏi database
            productRepository.delete(product);

            // --- Ghi log ---
            User currentUser = userService.getCurrentUser();
            historyLogService.logAction(
                    "Xóa vĩnh viễn sản phẩm: " + product.getName(),
                    currentUser,
                    "SUCCESS"
            );
        }
    }

    // --- Map DTO sang Entity ---
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

    // --- Lưu file ảnh ---
    private String saveFile(MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            if (originalName == null) throw new RuntimeException("File không hợp lệ");

            String extension = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
            if (!(extension.equals("jpg") || extension.equals("jpeg")
                    || extension.equals("png") || extension.equals("webp"))) {
                throw new RuntimeException("Chỉ hỗ trợ JPG, JPEG, PNG, WEBP");
            }

            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            String uniqueFileName = UUID.randomUUID() + "_" + originalName.replaceAll("\\s+", "_");
            Path path = Paths.get(UPLOAD_DIR + uniqueFileName);
            Files.write(path, file.getBytes());

            return "/uploads/" + uniqueFileName;
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file", e);
        }
    }

    // --- Xóa file ảnh ---
    private void deleteFile(String fileUrl) {
        if (fileUrl != null && fileUrl.startsWith("/uploads/")) {
            String filePath = UPLOAD_DIR + fileUrl.replace("/uploads/", "");
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                if (!file.delete()) {
                    System.err.println("Không thể xóa file: " + filePath);
                }
            }
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
