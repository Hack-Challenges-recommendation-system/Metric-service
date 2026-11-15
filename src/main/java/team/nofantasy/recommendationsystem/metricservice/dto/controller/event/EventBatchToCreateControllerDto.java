package team.nofantasy.recommendationsystem.metricservice.dto.controller.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record EventBatchToCreateControllerDto(
        @NotEmpty(message = "Send at least one event")
        @Size(max = 100, message = "Send at most 100 events at once")
        List<@Valid EventToCreateControllerDto> events
) {
}
