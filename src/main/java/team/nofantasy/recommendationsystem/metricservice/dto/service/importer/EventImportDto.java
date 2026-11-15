package team.nofantasy.recommendationsystem.metricservice.dto.service.importer;

import team.nofantasy.recommendationsystem.metricservice.entity.Event.EventType;

import java.time.Instant;

/**
 * One historical event in the import file: a JSON array of these objects.
 * The case-data service of the compose file writes it from the case dump; the mapping lives in
 * {@code case-data/events.sql} of the Documentation repository.
 */
public record EventImportDto(
        String userId,
        EventType type,
        String materialId,
        Instant occurredAt,
        String sessionId
) {
}
