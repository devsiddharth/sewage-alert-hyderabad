package com.sewagealert.notification.consumer;

import com.sewagealert.notification.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

// DeadLetterQueueConsumer: Observes notification.dlq — the parking lot for messages that
// permanently failed after exhausting all retry attempts.
//
// Responsibilities:
//   • Log the failure for monitoring/alerting (correlation via eventId when present).
//   • Optionally replay messages later (manual or via an ops tool) once the root cause is fixed.
//
// Future improvements: TTL-based automatic requeue, dead-letter metrics endpoint,
// and an admin UI to inspect/replay parked messages.
@Component
@RequiredArgsConstructor
@Slf4j
public class DeadLetterQueueConsumer {

    @RabbitListener(queues = "${app.rabbitmq.dlq-queue}")
    // onDeadLetter: Records each permanently failed message. Auto-acked (it is already dead —
    // requeueing here would only re-poison the queue).
    public void onDeadLetter(Message message) {
        String payload = new String(message.getBody(), StandardCharsets.UTF_8);
        String eventId = extractEventId(payload);

        log.error("Message moved to dead letter queue — eventId: {}, routingKey: {}, " +
                        "redelivered: {}, payload: {}",
                eventId,
                message.getMessageProperties() != null ? message.getMessageProperties().getReceivedRoutingKey() : null,
                message.getMessageProperties() != null ? message.getMessageProperties().isRedelivered() : null,
                payload);
    }

    // extractEventId: Best-effort pull of the eventId field for correlation in logs
    private String extractEventId(String payload) {
        try {
            Map<String, Object> parsed = JsonUtils.fromJson(payload);
            Object id = parsed.get("eventId");
            return id != null ? String.valueOf(id) : "unknown";
        } catch (Exception ex) {
            return "unparseable";
        }
    }
}
