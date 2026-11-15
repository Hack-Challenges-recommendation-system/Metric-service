package team.nofantasy.recommendationsystem.metricservice.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import team.nofantasy.recommendationsystem.metricservice.dto.controller.metric.MaterialUsersControllerDto;
import team.nofantasy.recommendationsystem.metricservice.dto.controller.metric.VariantMetricsControllerDto;
import team.nofantasy.recommendationsystem.metricservice.mapper.MetricMapper;
import team.nofantasy.recommendationsystem.metricservice.service.MetricService;

import java.time.Instant;
import java.util.List;

/**
 * Aggregates only: no user ids leave this controller.
 */
@RestController
@RequestMapping("/metrics")
@RequiredArgsConstructor
@Slf4j
public class MetricController {
    private final MetricService metricService;
    private final MetricMapper metricMapper;

    /**
     * @param from inclusive, ISO-8601, e.g. {@code 2026-11-01T00:00:00Z}; all time if absent.
     * @param to exclusive; now if absent.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/experiment")
    public List<VariantMetricsControllerDto> getExperimentMetrics(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to
    ) {
        log.atInfo()
                .setMessage("API endpoint is called")
                .addKeyValue("API endpoint", "GET /metrics/experiment")
                .log();

        return metricMapper.variantMetricsServiceToControllerDtoList(metricService.getExperimentMetrics(from, to));
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/materials/popular")
    public List<MaterialUsersControllerDto> getPopularMaterials(
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int limit
    ) {
        log.atInfo()
                .setMessage("API endpoint is called")
                .addKeyValue("API endpoint", "GET /metrics/materials/popular")
                .log();

        return metricMapper.materialUsersServiceToControllerDtoList(metricService.getPopularMaterials(limit));
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/materials/{materialId}/next")
    public List<MaterialUsersControllerDto> getMaterialsTakenNext(
            @PathVariable String materialId,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit
    ) {
        log.atInfo()
                .setMessage("API endpoint is called")
                .addKeyValue("API endpoint", "GET /metrics/materials/{materialId}/next")
                .addKeyValue("materialId", materialId)
                .log();

        return metricMapper.materialUsersServiceToControllerDtoList(metricService.getMaterialsTakenNext(materialId, limit));
    }
}
