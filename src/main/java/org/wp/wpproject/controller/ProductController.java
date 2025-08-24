package org.wp.wpproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.wp.wpproject.dto.ProductDTO;
import org.wp.wpproject.entity.Product;
import org.wp.wpproject.service.ProductService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired private ProductService productService;

    // --- Lấy danh sách tất cả sản phẩm ---
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts()
                .stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    // --- Lấy sản phẩm theo ID ---
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable String id) {
        return productService.getProductById(id)
                .map(ProductDTO::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- Tạo sản phẩm mới ---
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createProduct(
            @RequestPart("product") ProductDTO dto,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        if (dto.getImportPrice() == null) {
            return ResponseEntity.badRequest().body("Import price cannot be null");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Product name cannot be empty");
        }

        try {
            Product created = productService.createProduct(dto, imageFile);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ProductDTO(created));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving product: " + e.getMessage());
        }
    }

    // --- Cập nhật sản phẩm ---
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateProduct(
            @PathVariable String id,
            @RequestPart("product") ProductDTO dto,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        try {
            return productService.updateProduct(id, dto, imageFile)
                    .map(ProductDTO::new)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating product: " + e.getMessage());
        }
    }

    // --- Ẩn sản phẩm (soft delete) ---
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable String id) {
        boolean result = productService.hideProduct(id);
        if (result) {
            return ResponseEntity.ok("Product hidden successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
