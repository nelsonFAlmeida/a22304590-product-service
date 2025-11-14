package pt.ulusofona.cd.store.repository;

import feign.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ulusofona.cd.store.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findBySku(String sku);
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    List<Product> findBySupplierId(UUID supplierId);
    Long countBySupplierIdAndIsDiscontinued(UUID supplierId, Boolean isDiscontinued);

    @Transactional
    @Modifying
    @Query("UPDATE Product p SET p.isDiscontinued = true WHERE p.supplierId = :supplierId")
    int setProductsInactiveBySupplierId(@Param("supplierId") UUID supplierId);
}