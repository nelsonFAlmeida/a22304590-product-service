package pt.ulusofona.cd.store.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.ulusofona.cd.store.dto.MessageEnvelope;

/**
 * Generic utility to convert between MessageEnvelope representations.
 * Handles conversion from raw JSON strings to typed MessageEnvelope objects
 * and between different payload types within MessageEnvelope.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageEnvelopeConverter {

    private final ObjectMapper objectMapper;

    /**
     * Converts a raw JSON string to a strongly-typed MessageEnvelope.
     *
     * @param json The JSON string to convert
     * @param payloadType The target payload type class
     * @param <T> The payload type
     * @return A MessageEnvelope with the specified payload type
     * @throws IllegalArgumentException if JSON parsing fails
     */
    public <T> MessageEnvelope<T> convertFromJson(String json, Class<T> payloadType) {
        try {
            // First, deserialize to a raw MessageEnvelope with Object payload
            TypeReference<MessageEnvelope<Object>> typeRef = new TypeReference<>() {};
            MessageEnvelope<Object> rawEnvelope = objectMapper.readValue(json, typeRef);

            // Then convert the payload to the target type
            T typedPayload = objectMapper.convertValue(rawEnvelope.getPayload(), payloadType);

            // Create a new envelope with the typed payload
            return new MessageEnvelope<>(
                    rawEnvelope.getMessageId(),
                    rawEnvelope.getType(),
                    rawEnvelope.getTimestamp(),
                    rawEnvelope.getCorrelationId(),
                    rawEnvelope.getCausationId(),
                    typedPayload
            );

        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON message: {}", json, e);
            throw new IllegalArgumentException("Invalid message format", e);
        } catch (Exception e) {
            log.error("Error converting message: {}", json, e);
            throw new RuntimeException("Error processing message", e);
        }
    }

    /**
     * Converts the payload in a MessageEnvelope to the specified type.
     * If the payload is already the correct type, it returns it as-is.
     * If it's a Map (like LinkedHashMap), it converts it to the target type.
     *
     * @param envelope The message envelope with potentially unconverted payload
     * @param payloadType The target class type for the payload
     * @param <T> The payload type
     * @return A new MessageEnvelope with properly typed payload
     */
    public <T> MessageEnvelope<T> convertPayload(MessageEnvelope<?> envelope, Class<T> payloadType) {
        if (envelope == null) {
            return null;
        }

        Object rawPayload = envelope.getPayload();
        T typedPayload = convertRawPayload(rawPayload, payloadType);

        return new MessageEnvelope<>(
                envelope.getMessageId(),
                envelope.getType(),
                envelope.getTimestamp(),
                envelope.getCorrelationId(),
                envelope.getCausationId(),
                typedPayload
        );
    }

    /**
     * Extracts and converts just the payload from a MessageEnvelope.
     *
     * @param envelope The message envelope
     * @param payloadType The target class type for the payload
     * @param <T> The payload type
     * @return The properly typed payload, or null if the envelope is null
     */
    public <T> T extractPayload(MessageEnvelope<?> envelope, Class<T> payloadType) {
        if (envelope == null) {
            return null;
        }
        return convertRawPayload(envelope.getPayload(), payloadType);
    }

    /**
     * Converts a raw payload object to the specified type.
     * Handles null values and already-correct types.
     */
    private <T> T convertRawPayload(Object rawPayload, Class<T> targetType) {
        if (rawPayload == null) {
            return null;
        } else if (targetType.isInstance(rawPayload)) {
            return targetType.cast(rawPayload);
        } else {
            return objectMapper.convertValue(rawPayload, targetType);
        }
    }
}