package team.nofantasy.recommendationsystem.metricservice.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import team.nofantasy.recommendationsystem.metricservice.dto.controller.event.EventBatchToCreateControllerDto;
import team.nofantasy.recommendationsystem.metricservice.dto.controller.event.EventControllerDto;
import team.nofantasy.recommendationsystem.metricservice.mapper.EventMapper;
import team.nofantasy.recommendationsystem.metricservice.service.EventService;

import java.util.List;
import java.util.UUID;

/**
 * Events of the signed-in user. The API gateway puts the user id from the access token into {@code X-User-Id}.
 */
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {
    private final EventService eventService;
    private final EventMapper eventMapper;

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping
    public void recordEvents(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody @Valid EventBatchToCreateControllerDto eventBatch
    ) {
        log.atInfo()
                .setMessage("API endpoint is called")
                .addKeyValue("API endpoint", "POST /events")
                .addKeyValue("userId", userId)
                .addKeyValue("count", eventBatch.events().size())
                .log();

        eventService.recordEvents(userId, eventMapper.eventToCreateControllerToServiceDtoList(eventBatch.events()));
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/me")
    public List<EventControllerDto> getMyHistory(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(defaultValue = "50") @Min(1) @Max(500) int limit
    ) {
        log.atInfo()
                .setMessage("API endpoint is called")
                .addKeyValue("API endpoint", "GET /events/me")
                .addKeyValue("userId", userId)
                .log();

        return eventMapper.eventServiceToControllerDtoList(eventService.getHistory(userId, limit));
    }
}
