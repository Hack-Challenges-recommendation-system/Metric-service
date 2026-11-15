package team.nofantasy.recommendationsystem.metricservice.dto.service.importer;

import team.nofantasy.recommendationsystem.metricservice.entity.Event.EventType;

import java.time.Instant;

/**
 * One historical event in the import file: a JSON array of these objects.
 * The case data has its own layout; convert it to this format, or add an adapter in the importer package.
 */
public record EventImportDto(
        String userId,
        EventType type,
        String materialId,
        Instant occurredAt,
        String sessionId
) {
}
