package pt.ulusofona.cd.store.events;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pt.ulusofona.cd.store.dto.SupplierDeactivatedEvent;
import pt.ulusofona.cd.store.service.ProductService;

@Component
@RequiredArgsConstructor
public class SupplierEventConsumer {

    private final ProductService productService;

    @KafkaListener(topics = "${supplier.events.supplier-deactivated-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(SupplierDeactivatedEvent event) {
        String supplierId = event.getSupplierId();

        System.out.println("Received Supplier Deactivated Event for Supplier ID: " + supplierId);
        int updatedCount = productService.setProductsInactiveBySupplierId(supplierId);
        System.out.println("Successfully set " + updatedCount + " products to inactive for supplier " + supplierId);
    }
}