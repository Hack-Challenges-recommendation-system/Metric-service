package team.nofantasy.recommendationsystem.metricservice.service;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.nofantasy.recommendationsystem.metricservice.dto.service.metric.MaterialUsersServiceDto;
import team.nofantasy.recommendationsystem.metricservice.dto.service.metric.VariantMetricsServiceDto;
import team.nofantasy.recommendationsystem.metricservice.repository.EventRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MetricService {
    private final EventRepository eventRepository;
    private final Clock clock;

    /**
     * A/B metrics per variant for events in {@code [from, to)}; by default over all time up to now.
     */
    public List<VariantMetricsServiceDto> getExperimentMetrics(@Nullable Instant from, @Nullable Instant to) {
        Instant start = from == null ? Instant.EPOCH : from;
        Instant end = to == null ? clock.instant().plus(Duration.ofMinutes(1)) : to;

        return eventRepository.countByVariant(start, end).stream()
                .map(MetricService::toVariantMetrics)
                .toList();
    }

    public List<MaterialUsersServiceDto> getPopularMaterials(int limit) {
        return eventRepository.findPopular(limit).stream()
                .map(row -> new MaterialUsersServiceDto(row.getMaterialId(), row.getUsers()))
                .toList();
    }

    /**
     * Materials users took after this one; recommendation-service uses it as the "often next" signal.
     */
    public List<MaterialUsersServiceDto> getMaterialsTakenNext(String materialId, int limit) {
        return eventRepository.findTakenNext(materialId, limit).stream()
                .map(row -> new MaterialUsersServiceDto(row.getMaterialId(), row.getUsers()))
                .toList();
    }

    static VariantMetricsServiceDto toVariantMetrics(EventRepository.VariantCounts counts) {
        long impressions = counts.getImpressions();
        long clicks = counts.getClicks();
        long starts = counts.getStarts();
        long completions = counts.getCompletions();
        long users = counts.getUsers();
        long returningUsers = counts.getReturningUsers();

        return new VariantMetricsServiceDto(
                counts.getVariant(),
                impressions,
                clicks,
                starts,
                completions,
                users,
                returningUsers,
                rate(clicks, impressions),
                rate(starts, impressions),
                rate(completions, starts),
                rate(returningUsers, users)
        );
    }

    static double rate(long part, long whole) {
        return whole == 0 ? 0.0 : (double) part / whole;
    }
}
