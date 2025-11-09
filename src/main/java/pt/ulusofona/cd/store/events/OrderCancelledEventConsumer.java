package pt.ulusofona.cd.store.events;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import pt.ulusofona.cd.store.dto.MessageEnvelope;
import pt.ulusofona.cd.store.dto.OrderCancelledEvent;
import pt.ulusofona.cd.store.dto.OrderConfirmedEvent;
import pt.ulusofona.cd.store.dto.SupplierDeactivatedEvent;
import pt.ulusofona.cd.store.service.ProductService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderCancelledEventConsumer {
    private final ProductService productService;

    @RetryableTopic(
            attempts = "3",                             // total = 3 (1 + 2 retries)
            backoff = @Backoff(delay = 3000, multiplier = 2.0),
            dltTopicSuffix = ".DLT"                     // suffix for the DLT
    )
    @KafkaListener(
            topics = "${order.events.order-cancelled-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listenOrderCancelledEvent(MessageEnvelope<OrderCancelledEvent> message) {
        String productId = message.getPayload().getProductId();
        System.out.println("Received Order Cancelled Event for Product ID: " + productId);
        productService.addStock(UUID.fromString(productId), message.getPayload().getQuantity());
        System.out.println("Successfully added stock for product " + productId + " due to order cancellation.");
    }
    @DltHandler
    public void handleDlt(MessageEnvelope<OrderCancelledEvent> failedMessage) {
        System.err.println("Moved to DLT: " + failedMessage.getPayload().getOrderId());
        System.err.println("Envelope details: " + failedMessage);
        // Optionally alert or persist this failed message for manual review
    }
}
