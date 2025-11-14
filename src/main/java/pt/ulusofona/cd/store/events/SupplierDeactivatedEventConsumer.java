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
import pt.ulusofona.cd.store.dto.SupplierDeactivatedEvent; // Alterado de Payload para Event
import pt.ulusofona.cd.store.service.ProductService;
import pt.ulusofona.cd.store.util.MessageEnvelopeConverter;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupplierDeactivatedEventConsumer {

    private final ProductService productService;
    private final MessageEnvelopeConverter messageConverter;
    private final ObjectMapper objectMapper;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 3000, multiplier = 2.0),
            dltTopicSuffix = ".DLT"
    )
    @KafkaListener(
            topics = "${supplier.events.supplier-deactivated-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(String rawMessage) { // Nome do método alterado para 'listen' como no original
        try {
            log.info("Received raw message: {}", rawMessage);

            // Converter mensagem JSON bruta para MessageEnvelope tipado
            MessageEnvelope<SupplierDeactivatedEvent> message =
                    messageConverter.convertFromJson(rawMessage, SupplierDeactivatedEvent.class);

            log.info("Processed message: {} for supplier {}", message.getType(), message.getPayload().getSupplierId());
            log.info("Correlation ID: {}", message.getCorrelationId());
            log.info("Timestamp: {}", message.getTimestamp());

            String supplierId = message.getPayload().getSupplierId();
            if (supplierId.startsWith("FAIL")) {
                throw new RuntimeException("Simulated failure for " + supplierId);
            }

            log.info("Processing Supplier Deactivated Event for Supplier ID: {}", supplierId);
            int updatedCount = productService.setProductsInactiveBySupplierId(supplierId);
            log.info("Successfully set {} products to inactive for supplier {}", updatedCount, supplierId);

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
                MessageEnvelope<SupplierDeactivatedEvent> message =
                        messageConverter.convertFromJson(rawMessage, SupplierDeactivatedEvent.class);
                log.error("DLT - Supplier ID: {}", message.getPayload().getSupplierId());
            } catch (Exception e) {
                log.error("Could not extract supplier ID from DLT message", e);
            }
        } catch (Exception e) {
            log.error("Error in DLT handler", e);
        }
    }
}