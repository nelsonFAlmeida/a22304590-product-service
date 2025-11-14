package pt.ulusofona.cd.store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ulusofona.cd.store.client.OrderClient;
import pt.ulusofona.cd.store.client.SupplierClient;
import pt.ulusofona.cd.store.dto.ProductRequest;
import pt.ulusofona.cd.store.dto.SupplierDto;
import pt.ulusofona.cd.store.exception.ProductNotFoundException;
import pt.ulusofona.cd.store.mapper.ProductMapper;
import pt.ulusofona.cd.store.model.Product;
import pt.ulusofona.cd.store.repository.ProductRepository;
import pt.ulusofona.cd.store.client.OrderClient;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final SupplierClient supplierClient;
    private final OrderClient orderClient;

    @Transactional
    public Product createProduct(ProductRequest request) {
        if (request.getSupplierId() != null) {
            try {
                SupplierDto supplier = supplierClient.getSupplierById(request.getSupplierId().toString());
                if (!supplier.isActive()) {
                    throw new IllegalArgumentException("Supplier is not active");
                }
            } catch (Exception e) {
                System.out.println(e);
                throw new IllegalArgumentException("Invalid supplier ID: " + request.getSupplierId());
            }
        }
        Product product = ProductMapper.toEntity(request);
        return productRepository.save(product);
    }

    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    }

    @Transactional
    public Product removeStock(UUID id, Integer requested) {
        Product product;

        try  {
            product = getProductById(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Product not found!");
        }

        int available = product.getStock();

        if (available < requested) {
            throw new IllegalArgumentException(
                    "Insufficient stock for product " + product.getId() +
                            " (requested " + requested + ", available " + available + ")"
            );
        }

        product.setStock(product.getStock() - requested);

        return productRepository.save(product);
    }

    @Transactional
    public Product addStock(UUID id, Integer requested) {
        Product product;

        try  {
            product = getProductById(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Product not found!");
        }

        product.setStock(product.getStock() + requested);

        return productRepository.save(product);
    }

    @Transactional()
    public long countProductsBySupplierAndDiscontinued(UUID supplierId, boolean isDiscontinued) {
        return productRepository.countBySupplierIdAndIsDiscontinued(supplierId, isDiscontinued);
    }

    @Transactional
    public Product discontinueProduct(UUID id) {
        Product product = getProductById(id);

        boolean productHasPendingOrder;
        try {
            productHasPendingOrder = orderClient.productHasPendingOrder(id);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao comunicar com o serviço de encomendas", e);
        }

        if (productHasPendingOrder) {
            throw new IllegalStateException("Não é possível descontinuar: existem encomendas pendentes");
        }

        product.setIsDiscontinued(true);
        return productRepository.save(product);
    }


    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsBySupplier(UUID supplierId) {
        return productRepository.findBySupplierId(supplierId);
    }

    @Transactional
    public Product updateProduct(UUID id, ProductRequest productDetails) {
        Product product = getProductById(id);

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setSku(productDetails.getSku());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());
        product.setCurrency(productDetails.getCurrency());

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }


    //TODO o modo de Alterar estado de um supplier pode ser melhorado para ser mais seguro e mais eficiente usando pedidos maiores em vez de varios pedidos
    public Boolean supplierHasBlockedProducts(UUID supplierId) {
        List<Product> products = getProductsBySupplier(supplierId);

        if (products == null || products.isEmpty()) {
            return false;
        }


        // enventualmente fazer isto tudo num so pedido
        //** verificar se algum produto nao descontinuado tem encomendas pendentes
        for (Product product : products) {
            if (Boolean.FALSE.equals(product.getIsDiscontinued())) {
                boolean hasPending = orderClient.productHasPendingOrder(product.getId());
                if (hasPending) {
                    return true;
                }
            }
        }
        return false;
    }

    @Transactional
    public void discontinueProductsBySupplier(UUID supplierId) { // so pode ser usada depois do supplierHasBlockedProducts() porque ja foi feita uma verificação antes
        List<Product> products = productRepository.findBySupplierId(supplierId);

        for (Product product : products) {
            if (!Boolean.TRUE.equals(product.getIsDiscontinued())) {
                product.setIsDiscontinued(true);
                productRepository.save(product);
            }
        }
    }

    public int setProductsInactiveBySupplierId(String id){
        UUID supplierUuid = UUID.fromString(id);
        return productRepository.setProductsInactiveBySupplierId(supplierUuid);
    }

}