package team.nofantasy.recommendationsystem.metricservice.service;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import team.nofantasy.recommendationsystem.metricservice.dto.service.metric.VariantMetricsServiceDto;
import team.nofantasy.recommendationsystem.metricservice.entity.Event;
import team.nofantasy.recommendationsystem.metricservice.repository.EventRepository;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.ToDoubleFunction;

@Component
@Slf4j
public class BusinessMetrics {
    private static final String[] VARIANTS = {"llm", "baseline"};

    private final EventRepository eventRepository;
    private final MetricService metricService;
    private volatile Snapshot snapshot = new Snapshot(Map.of(), Map.of(), false);

    public BusinessMetrics(EventRepository eventRepository, MetricService metricService, MeterRegistry registry) {
        this.eventRepository = eventRepository;
        this.metricService = metricService;

        for (Event.EventType type : Event.EventType.values()) {
            for (Event.EventSource source : Event.EventSource.values()) {
                Gauge.builder("business.events", this, metrics -> metrics.events(type, source))
                        .tags("type", type.name().toLowerCase(Locale.ROOT), "source", source.name().toLowerCase(Locale.ROOT))
                        .description("Persisted user events, including imported and synthetic data")
                        .register(registry);
            }
        }

        for (String variant : VARIANTS) {
            registerVariant(registry, "business.experiment.impressions", variant, VariantMetricsServiceDto::impressions);
            registerVariant(registry, "business.experiment.clicks", variant, VariantMetricsServiceDto::clicks);
            registerVariant(registry, "business.experiment.starts", variant, VariantMetricsServiceDto::starts);
            registerVariant(registry, "business.experiment.completions", variant, VariantMetricsServiceDto::completions);
            registerVariant(registry, "business.experiment.users", variant, VariantMetricsServiceDto::users);
            registerVariant(registry, "business.experiment.returning.users", variant, VariantMetricsServiceDto::returningUsers);
            registerVariant(registry, "business.experiment.ctr", variant, VariantMetricsServiceDto::clickThroughRate);
            registerVariant(registry, "business.experiment.start.rate", variant, VariantMetricsServiceDto::startRate);
            registerVariant(registry, "business.experiment.completion.rate", variant, VariantMetricsServiceDto::completionRate);
            registerVariant(registry, "business.experiment.return.rate", variant, VariantMetricsServiceDto::returnRate);
        }

        Gauge.builder("business.snapshot.success", this, metrics -> metrics.snapshot.success() ? 1 : 0)
                .description("Whether the latest business metrics refresh succeeded")
                .register(registry);
    }

    @Scheduled(initialDelayString = "5000", fixedDelayString = "${app.observability.refresh-ms:30000}")
    public void refresh() {
        try {
            Map<String, Long> events = new HashMap<>();
            for (EventRepository.EventCounts row : eventRepository.countByTypeAndSource()) {
                events.put(row.getEventType() + ":" + row.getEventSource(), row.getEventCount());
            }

            Map<String, VariantMetricsServiceDto> variants = new HashMap<>();
            for (VariantMetricsServiceDto row : metricService.getExperimentMetrics(null, null)) {
                if ("llm".equals(row.variant()) || "baseline".equals(row.variant())) {
                    variants.put(row.variant(), row);
                }
            }
            snapshot = new Snapshot(Map.copyOf(events), Map.copyOf(variants), true);
        } catch (RuntimeException error) {
            snapshot = new Snapshot(snapshot.events(), snapshot.variants(), false);
            log.atWarn().setMessage("Business metrics refresh failed").setCause(error).log();
        }
    }

    private void registerVariant(
            MeterRegistry registry,
            String name,
            String variant,
            ToDoubleFunction<VariantMetricsServiceDto> value
    ) {
        Gauge.builder(name, this, metrics -> metrics.variant(variant, value))
                .tag("variant", variant)
                .register(registry);
    }

    private double events(Event.EventType type, Event.EventSource source) {
        return snapshot.events().getOrDefault(type.name() + ":" + source.name(), 0L);
    }

    private double variant(String variant, ToDoubleFunction<VariantMetricsServiceDto> value) {
        VariantMetricsServiceDto row = snapshot.variants().get(variant);
        return row == null ? 0 : value.applyAsDouble(row);
    }

    private record Snapshot(
            Map<String, Long> events,
            Map<String, VariantMetricsServiceDto> variants,
            boolean success
    ) {
    }
}
