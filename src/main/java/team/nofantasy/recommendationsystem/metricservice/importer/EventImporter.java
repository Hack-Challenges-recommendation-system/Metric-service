package team.nofantasy.recommendationsystem.metricservice.importer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import team.nofantasy.recommendationsystem.metricservice.config.EventsProperties;
import team.nofantasy.recommendationsystem.metricservice.dto.service.importer.EventImportDto;
import team.nofantasy.recommendationsystem.metricservice.entity.Event;
import team.nofantasy.recommendationsystem.metricservice.repository.EventRepository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * Loads the historical interaction log once: if imported events already exist, the import is skipped.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventImporter {
    private final EventsProperties eventsProperties;
    private final ResourceLoader resourceLoader;
    private final JsonMapper jsonMapper;
    private final EventRepository eventRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void importOnStartup() {
        String location = eventsProperties.importPath();

        if (location == null || location.isBlank()) {
            log.atInfo().setMessage("Event import is skipped: no import path").log();
            return;
        }
        if (eventRepository.existsBySource(Event.EventSource.IMPORT)) {
            log.atInfo().setMessage("Event import is skipped: events are already imported").log();
            return;
        }

        Resource resource = resourceLoader.getResource(location);
        if (!resource.exists()) {
            log.atWarn().setMessage("Event import file is not found").addKeyValue("location", location).log();
            return;
        }

        int imported = importFrom(resource);
        log.atInfo().setMessage("Events are imported").addKeyValue("events", imported).log();
    }

    /**
     * @return how many events were saved; rows without user, type, material or time are skipped.
     */
    public int importFrom(Resource resource) {
        List<EventImportDto> rows;
        try (InputStream input = resource.getInputStream()) {
            rows = jsonMapper.readValue(input, new TypeReference<>() {});
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read events file " + resource.getDescription(), e);
        }

        List<Event> events = rows.stream()
                .filter(row -> row.userId() != null && !row.userId().isBlank()
                        && row.type() != null
                        && row.materialId() != null && !row.materialId().isBlank()
                        && row.occurredAt() != null)
                .map(row -> Event.builder()
                        .userId(row.userId())
                        .sessionId(row.sessionId())
                        .type(row.type())
                        .materialId(row.materialId())
                        .occurredAt(row.occurredAt())
                        .source(Event.EventSource.IMPORT)
                        .build())
                .toList();

        eventRepository.saveAll(events);
        return events.size();
    }
}
