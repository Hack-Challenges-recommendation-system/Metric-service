package team.nofantasy.recommendationsystem.metricservice.service;

import org.junit.jupiter.api.Test;
import team.nofantasy.recommendationsystem.metricservice.dto.service.metric.VariantMetricsServiceDto;
import team.nofantasy.recommendationsystem.metricservice.repository.EventRepository;

import static org.assertj.core.api.Assertions.assertThat;

class MetricServiceTest {
    private static EventRepository.VariantCounts counts(long impressions, long clicks, long starts, long completions,
                                                        long users, long returningUsers) {
        return new EventRepository.VariantCounts() {
            @Override public String getVariant() { return "A"; }
            @Override public Long getImpressions() { return impressions; }
            @Override public Long getClicks() { return clicks; }
            @Override public Long getStarts() { return starts; }
            @Override public Long getCompletions() { return completions; }
            @Override public Long getUsers() { return users; }
            @Override public Long getReturningUsers() { return returningUsers; }
        };
    }

    @Test
    void computesRatesFromCounts() {
        VariantMetricsServiceDto metrics = MetricService.toVariantMetrics(counts(200, 30, 20, 5, 50, 10));

        assertThat(metrics.clickThroughRate()).isEqualTo(0.15);
        assertThat(metrics.startRate()).isEqualTo(0.1);
        assertThat(metrics.completionRate()).isEqualTo(0.25);
        assertThat(metrics.returnRate()).isEqualTo(0.2);
    }

    @Test
    void rateIsZeroWithoutDenominator() {
        VariantMetricsServiceDto metrics = MetricService.toVariantMetrics(counts(0, 0, 0, 0, 0, 0));

        assertThat(metrics.clickThroughRate()).isZero();
        assertThat(metrics.startRate()).isZero();
        assertThat(metrics.completionRate()).isZero();
        assertThat(metrics.returnRate()).isZero();
    }
}
