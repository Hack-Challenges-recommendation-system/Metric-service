package team.nofantasy.recommendationsystem.metricservice.dto.service.metric;

public record VariantMetricsServiceDto(
        String variant,
        long impressions,
        long clicks,
        long starts,
        long completions,
        long users,
        long returningUsers,
        double clickThroughRate,
        double startRate,
        double completionRate,
        double returnRate
) {
}
