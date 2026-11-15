package team.nofantasy.recommendationsystem.metricservice.dto.service.event;

import team.nofantasy.recommendationsystem.metricservice.entity.Event.EventType;

import java.time.Instant;
import java.util.UUID;

public record EventServiceDto(
        EventType type,
        String materialId,
        UUID recommendationId,
        String variant,
        Integer position,
        String surface,
        Instant occurredAt
) {
}
