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
import pt.ulusofona.cd.store.dto.ReportProductionEvent;
import pt.ulusofona.cd.store.service.ProductService;
import pt.ulusofona.cd.store.util.MessageEnvelopeConverter;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportProductionConsumer {

    private final ProductService productService;
    private final MessageEnvelopeConverter messageConverter;
    private final ObjectMapper objectMapper;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 3000, multiplier = 2.0),
            dltTopicSuffix = ".DLT"
    )
    @KafkaListener(
            topics = "${supplier.events.report-production-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listenReportProduction(String rawMessage) {
        try {
            log.info("Received raw message: {}", rawMessage);

            // Converter mensagem JSON bruta para MessageEnvelope tipado
            MessageEnvelope<ReportProductionEvent> message =
                    messageConverter.convertFromJson(rawMessage, ReportProductionEvent.class);

            log.info("Processed message: {} for supplier {}", message.getType(), message.getPayload().getSupplierId());
            log.info("Correlation ID: {}", message.getCorrelationId());
            log.info("Timestamp: {}", message.getTimestamp());

            // Simulate transient failure for testing
            if (message.getPayload().getSupplierId().startsWith("FAIL")) {
                throw new RuntimeException("Simulated failure for " + message.getPayload().getSupplierId());
            }

            log.info("Processing Report Production Event for Order ID: {}", message.getPayload().getOrderId());

            productService.addStock(UUID.fromString(message.getPayload().getOrderId()),
                    message.getPayload().getQuantity());

            log.info("Updated stock for Order ID: {} by quantity: {}",
                    message.getPayload().getOrderId(), message.getPayload().getQuantity());

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
                MessageEnvelope<ReportProductionEvent> message =
                        messageConverter.convertFromJson(rawMessage, ReportProductionEvent.class);
                log.error("DLT - Supplier ID: {}", message.getPayload().getSupplierId());
            } catch (Exception e) {
                log.error("Could not extract supplier ID from DLT message", e);
            }
        } catch (Exception e) {
            log.error("Error in DLT handler", e);
        }
    }
}