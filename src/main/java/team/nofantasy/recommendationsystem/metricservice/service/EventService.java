package team.nofantasy.recommendationsystem.metricservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.nofantasy.recommendationsystem.metricservice.dto.service.event.EventServiceDto;
import team.nofantasy.recommendationsystem.metricservice.dto.service.event.EventToCreateServiceDto;
import team.nofantasy.recommendationsystem.metricservice.entity.Event;
import team.nofantasy.recommendationsystem.metricservice.mapper.EventMapper;
import team.nofantasy.recommendationsystem.metricservice.repository.EventRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final Clock clock;

    /**
     * Stores events sent by the frontend for this user.
     */
    @Transactional
    public void recordEvents(UUID userId, List<EventToCreateServiceDto> events) {
        Instant now = clock.instant();

        eventRepository.saveAll(events.stream()
                .map(event -> Event.builder()
                        .userId(userId.toString())
                        .sessionId(event.sessionId())
                        .type(event.type())
                        .materialId(event.materialId())
                        .recommendationId(event.recommendationId())
                        .variant(event.variant())
                        .position(event.position())
                        .surface(event.surface())
                        .occurredAt(event.occurredAt() == null ? now : event.occurredAt())
                        .source(Event.EventSource.APP)
                        .build())
                .toList());
    }

    /**
     * @return the latest events of the user, newest first.
     */
    @Transactional(readOnly = true)
    public List<EventServiceDto> getHistory(UUID userId, int limit) {
        return eventRepository.findByUserIdOrderByOccurredAtDesc(userId.toString(), Limit.of(limit)).stream()
                .map(eventMapper::eventEntityToServiceDto)
                .toList();
    }
}
