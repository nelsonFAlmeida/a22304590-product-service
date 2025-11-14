package pt.ulusofona.cd.store.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import pt.ulusofona.cd.store.dto.MessageEnvelope;
import pt.ulusofona.cd.store.dto.OrderConfirmedEvent;
import pt.ulusofona.cd.store.service.ProductService;
import pt.ulusofona.cd.store.util.MessageEnvelopeConverter;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConfirmedEventConsumer {

    private final ProductService productService;
    private final MessageEnvelopeConverter messageConverter;
    private final ObjectMapper objectMapper;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 3000, multiplier = 2.0),
            dltTopicSuffix = ".DLT"
    )
    @KafkaListener(
            topics = "${order.events.order-confirmed-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listenOrderConfirmedEvent(String rawMessage) {
        try {
            log.info("Received raw message: {}", rawMessage);

            // Converter mensagem JSON bruta para MessageEnvelope tipado
            MessageEnvelope<OrderConfirmedEvent> message =
                    messageConverter.convertFromJson(rawMessage, OrderConfirmedEvent.class);

            log.info("Processed message: {} for order {}", message.getType(), message.getPayload().getOrderId());
            log.info("Correlation ID: {}", message.getCorrelationId());
            log.info("Timestamp: {}", message.getTimestamp());

            String productId = message.getPayload().getProductId();
            log.info("Processing Order Confirmed Event for Product ID: {}", productId);

            productService.removeStock(UUID.fromString(productId), message.getPayload().getQuantity());

            log.info("Successfully removed stock for product {} due to order confirmation.", productId);

        } catch (Exception e) {
            log.error("Error processing message: {}", rawMessage, e);
            throw e; // Permite ao mecanismo de retry tratar o erro
        }
    }

    @DltHandler
    public void handleDlt(String rawMessage) {
        try {
            log.error("Message moved to DLT: {}", rawMessage);

            // Tentativa de extração de informação útil para diagnóstico
            try {
                MessageEnvelope<OrderConfirmedEvent> message =
                        messageConverter.convertFromJson(rawMessage, OrderConfirmedEvent.class);
                log.error("DLT - Order ID: {}", message.getPayload().getOrderId());
            } catch (Exception e) {
                log.error("Could not extract order ID from DLT message", e);
            }
        } catch (Exception e) {
            log.error("Error in DLT handler", e);
        }
    }
}