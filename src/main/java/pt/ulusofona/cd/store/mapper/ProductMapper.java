package pt.ulusofona.cd.store.mapper;

import pt.ulusofona.cd.store.model.Product;
import pt.ulusofona.cd.store.dto.ProductRequest;
import pt.ulusofona.cd.store.dto.ProductResponse;

public class ProductMapper {

    public static Product toEntity(ProductRequest dto) {
        Product p = new Product();
        p.setName(dto.getName().trim());
        p.setDescription(dto.getDescription());
        p.setSku(dto.getSku().trim());
        p.setPrice(dto.getPrice());
        p.setStock(dto.getStock());
        p.setSupplierId(dto.getSupplierId());
        p.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "EUR");
        return p;
    }

    public static ProductResponse toResponse(Product entity) {
        ProductResponse r = new ProductResponse();
        r.setId(entity.getId());
        r.setName(entity.getName());
        r.setDescription(entity.getDescription());
        r.setSku(entity.getSku());
        r.setPrice(entity.getPrice());
        r.setStock(entity.getStock());
        r.setCurrency(entity.getCurrency());
        return r;
    }
}