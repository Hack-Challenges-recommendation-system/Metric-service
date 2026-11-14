package team.nofantasy.recommendationsystem.metricservice.dto.controller.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;
import team.nofantasy.recommendationsystem.metricservice.entity.Event.EventType;

import java.time.Instant;
import java.util.UUID;

/**
 * One event from the frontend. The user comes from {@code X-User-Id}, never from the body.
 */
public record EventToCreateControllerDto(
        @NotNull(message = "Event type is required")
        EventType type,

        @NotBlank(message = "Material id is required")
        @Size(max = 64)
        String materialId,

        @Nullable
        UUID recommendationId,

        @Nullable
        @Size(max = 32)
        String variant,

        @Nullable
        @PositiveOrZero
        Integer position,

        @Nullable
        @Size(max = 32)
        String surface,

        @Nullable
        @Size(max = 64)
        String sessionId,

        /*
         * When it happened on the client; the server time is used if absent.
         */
        @Nullable
        Instant occurredAt
) {
}
