package pt.ulusofona.cd.store.events;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pt.ulusofona.cd.store.dto.OrderCancelledEvent;
import pt.ulusofona.cd.store.dto.SupplierDeactivatedEvent;
import pt.ulusofona.cd.store.service.ProductService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderEventConsumer {
    private final ProductService productService;

    @KafkaListener(topics = "${order.events.order-cancelled-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenOrderCancelledEvent(OrderCancelledEvent event) {
        String productId = event.getProductId();
        System.out.println("Received Order Cancelled Event for Product ID: " + productId);
        productService.addStock(UUID.fromString(productId), event.getQuantity());
        System.out.println("Successfully added stock for product " + productId + " due to order cancellation.");
    }

    @KafkaListener(topics = "${order.events.order-confirmed-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenOrderConfirmedEvent(pt.ulusofona.cd.store.dto.OrderConfirmedEvent event) {
        String productId = event.getProductId();
        System.out.println("Received Order Confirmed Event for Product ID: " + productId);
        productService.removeStock(UUID.fromString(productId), event.getQuantity());
        System.out.println("Successfully removed stock for product " + productId + " due to order confirmation.");
    }

}
