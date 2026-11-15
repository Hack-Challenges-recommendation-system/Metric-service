package team.nofantasy.recommendationsystem.metricservice.dto.service.event;

import team.nofantasy.recommendationsystem.metricservice.entity.Event.EventType;

import java.time.Instant;
import java.util.UUID;

public record EventToCreateServiceDto(
        EventType type,
        String materialId,
        UUID recommendationId,
        String variant,
        Integer position,
        String surface,
        String sessionId,
        Instant occurredAt
) {
}
