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

    @Autowired
    private ProductService productService;

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

    // --- Lấy danh sách sản phẩm theo Location (khu vực) ---
    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<ProductDTO>> getProductsByLocation(@PathVariable Integer locationId) {
        List<ProductDTO> products = productService.getProductsByLocationId(locationId)
                .stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    // --- Tạo sản phẩm mới ---
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ProductDTO> createProduct(
            @RequestPart("product") ProductDTO dto,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        // Validation cơ bản
        if (dto.getImportPrice() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Product created = productService.createProduct(dto, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProductDTO(created));
    }

    // --- Cập nhật sản phẩm ---
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable String id,
            @RequestPart("product") ProductDTO dto,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        return productService.updateProduct(id, dto, imageFile)
                .map(ProductDTO::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- Ẩn sản phẩm (soft delete) ---
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable String id) {
        boolean result = productService.hideProduct(id);
        if (result) {
            return ResponseEntity.ok("Product hidden successfully");
        }
        return ResponseEntity.notFound().build();
    }
}
