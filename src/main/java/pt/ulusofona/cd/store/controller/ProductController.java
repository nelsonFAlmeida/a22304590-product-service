package pt.ulusofona.cd.store.controller;

import lombok.RequiredArgsConstructor;
import pt.ulusofona.cd.store.dto.ProductRequest;
import pt.ulusofona.cd.store.dto.ProductResponse;
import pt.ulusofona.cd.store.dto.Stock;
import pt.ulusofona.cd.store.model.Product;
import pt.ulusofona.cd.store.mapper.ProductMapper;
import pt.ulusofona.cd.store.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class    ProductController {

    private final ProductService service;

    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody ProductRequest request
    ) {
        Product created = service.createProduct(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProductMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable UUID id) {
        Product product = service.getProductById(id);
        return ResponseEntity.ok(ProductMapper.toResponse(product));
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<ProductResponse>> getBySupplier(@PathVariable String supplierId) {
        UUID id;
        try {
            id = UUID.fromString(supplierId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // 400 se o UUID for inválido
        }

        List<Product> products = service.getProductsBySupplier(id);
        List<ProductResponse> response = products.stream()
                .map(ProductMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll() {
        List<Product> products = service.getAllProducts();
        List<ProductResponse> responseList = products.stream()
                .map(ProductMapper::toResponse)
                .toList();

        return ResponseEntity.ok(responseList);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest request
    ) {
        Product updated = service.updateProduct(id, request);
        return ResponseEntity.ok(ProductMapper.toResponse(updated));
    }

    @PutMapping("/{id}/stock/remove")
    public ResponseEntity<ProductResponse> removeStock(
            @PathVariable UUID id,
            @Valid @RequestBody Stock request
    ) {
        Product updated = service.removeStock(id, request.quantity());
        return ResponseEntity.ok(ProductMapper.toResponse(updated));
    }

    @PutMapping("/{id}/stock/add")
    public ResponseEntity<ProductResponse> addStock(
            @PathVariable UUID id,
            @Valid @RequestBody Stock request
    ) {
        Product updated = service.addStock(id, request.quantity());
        return ResponseEntity.ok(ProductMapper.toResponse(updated));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/discontinue")
    public ResponseEntity<ProductResponse> discontinue(@PathVariable UUID id) {
        Product discontinued = service.discontinueProduct(id);
        return ResponseEntity.ok(ProductMapper.toResponse(discontinued));
    }

    @GetMapping("/{supplierId}/products/blocked")
    public Boolean supplierHasBlockedProducts(@PathVariable UUID supplierId) {
        return service.supplierHasBlockedProducts(supplierId);
    }

    @PutMapping("/suppliers/{supplierId}/products/discontinue")
    public ResponseEntity<Void> discontinueProductsBySupplier(@PathVariable UUID supplierId) {
        service.discontinueProductsBySupplier(supplierId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }



}