package pt.ulusofona.cd.store.events;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import pt.ulusofona.cd.store.dto.MessageEnvelope;
import pt.ulusofona.cd.store.dto.ReportProductionEvent;
import pt.ulusofona.cd.store.dto.SupplierDeactivatedEvent;
import pt.ulusofona.cd.store.service.ProductService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SupplierDeactivatedEventConsumer {

    private final ProductService productService;

    @RetryableTopic(
            attempts = "3",                             // total = 3 (1 + 2 retries)
            backoff = @Backoff(delay = 3000, multiplier = 2.0),
            dltTopicSuffix = ".DLT"                     // suffix for the DLT
    )
    @KafkaListener(
            topics = "${supplier.events.supplier-deactivated-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(MessageEnvelope<SupplierDeactivatedEvent> message) {
        String supplierId = message.getPayload().getSupplierId();
        System.out.println("Received Supplier Deactivated Event for Supplier ID: " + supplierId);

        // Simulate transient failure for testing
        if (message.getPayload().getSupplierId().startsWith("FAIL")) {
            throw new RuntimeException("(SupplierDeactivatedEvent)" +
                    "Simulated failure for " + message.getPayload().getSupplierId());
        }

        int updatedCount = productService.setProductsInactiveBySupplierId(supplierId);
        System.out.println("Successfully set " + updatedCount + " products to inactive for supplier " + supplierId);
    }
    @DltHandler
    public void handleDlt(MessageEnvelope<SupplierDeactivatedEvent> failedMessage) {
        System.err.println("Moved to DLT: " + failedMessage.getPayload().getSupplierId());
        System.err.println("Envelope details: " + failedMessage);
        // Optionally alert or persist this failed message for manual review
    }
}
