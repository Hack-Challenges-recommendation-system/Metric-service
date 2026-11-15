package team.nofantasy.recommendationsystem.metricservice.dto.controller.event;

import team.nofantasy.recommendationsystem.metricservice.entity.Event.EventType;

import java.time.Instant;
import java.util.UUID;

public record EventControllerDto(
        EventType type,
        String materialId,
        UUID recommendationId,
        String variant,
        Integer position,
        String surface,
        Instant occurredAt
) {
}
