package pt.ulusofona.cd.store.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 120, message = "Product name must be between 3 and 120 characters")
    private String name;

    private String description;

    @NotBlank(message = "SKU is required")
    @Size(max = 255, message = "SKU must be at most 255 characters long")
    private String sku;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be a positive number")
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    private int stock;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    private String currency = "EUR";

    private UUID supplierId;
}