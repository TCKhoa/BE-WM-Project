package org.wp.wpproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wp.wpproject.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private String id;
    private String productCode;
    private String name;


    private Integer categoryId;
    private String categoryName;

    private Integer brandId;
    private String brandName;

    private Integer unitId;
    private String unitName;

    private Integer locationId;
    private String locationName;

    private BigDecimal importPrice;
    private Integer stock;
    private String description;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductDTO(Product product) {
        this.id = product.getId();
        this.productCode = product.getProductCode();
        this.name = product.getName();

        this.categoryId = product.getCategory() != null ? product.getCategory().getId() : null;
        this.categoryName = product.getCategory() != null ? product.getCategory().getName() : null;

        this.brandId = product.getBrand() != null ? product.getBrand().getId() : null;
        this.brandName = product.getBrand() != null ? product.getBrand().getName() : null;

        this.unitId = product.getUnit() != null ? product.getUnit().getId() : null;
        this.unitName = product.getUnit() != null ? product.getUnit().getName() : null;

        this.locationId = product.getLocation() != null ? product.getLocation().getId() : null;
        this.locationName = product.getLocation() != null ? product.getLocation().getName() : null;

        this.importPrice = product.getImportPrice();
        this.stock = product.getStock();
        this.description = product.getDescription();
//        this.imageUrl = product.getImageUrl();
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
        if (product.getImageUrl() != null) {
            this.imageUrl = "http://localhost:8080" + product.getImageUrl();
        }
    }
}
