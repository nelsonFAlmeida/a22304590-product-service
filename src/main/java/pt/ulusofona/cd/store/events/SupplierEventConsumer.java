package pt.ulusofona.cd.store.events;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pt.ulusofona.cd.store.dto.ReportProductionEvent;
import pt.ulusofona.cd.store.dto.SupplierDeactivatedEvent;
import pt.ulusofona.cd.store.service.ProductService;

import java.util.UUID;

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

    @KafkaListener(topics = "${supplier.events.report-production-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenReportProduction(ReportProductionEvent event) {
        System.out.println("Received Report Production Event: " + event);
        // Here you can add logic to process the report production event if needed
        productService.addStock(UUID.fromString(event.getOrderId()), event.getQuantity());
        System.out.println("Updated stock for Order ID: " + event.getOrderId() + " by quantity: " + event.getQuantity());
    }
}