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
public class ReportProductionConsumer {

    private final ProductService productService;

    @RetryableTopic(
            attempts = "3",                             // total = 3 (1 + 2 retries)
            backoff = @Backoff(delay = 3000, multiplier = 2.0),
            dltTopicSuffix = ".DLT"                     // suffix for the DLT
    )
    @KafkaListener(
            topics = "${supplier.events.report-production-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listenReportProduction(MessageEnvelope<ReportProductionEvent> message) {
        System.out.println("Received Report Production Event: " + message);
        // Here you can add logic to process the report production event if needed

        // Simulate transient failure for testing
        if (message.getPayload().getSupplierId().startsWith("FAIL")) {
            throw new RuntimeException("(SupplierDeactivatedEvent)" +
                    "Simulated failure for " + message.getPayload().getSupplierId());
        }

        productService.addStock(UUID.fromString(message.getPayload().getOrderId()),
                message.getPayload().getQuantity());
        System.out.println("Updated stock for Order ID: " + message.getPayload().getOrderId() +
                " by quantity: " + message.getPayload().getQuantity());
    }
    @DltHandler
    public void handleDlt(MessageEnvelope<ReportProductionEvent> failedMessage) {
        System.err.println("Moved to DLT: " + failedMessage.getPayload().getSupplierId());
        System.err.println("Envelope details: " + failedMessage);
        // Optionally alert or persist this failed message for manual review
    }

}
