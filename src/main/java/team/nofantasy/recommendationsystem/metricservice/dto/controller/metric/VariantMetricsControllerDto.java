package team.nofantasy.recommendationsystem.metricservice.dto.controller.metric;

/**
 * A/B metrics of one variant. Rates are 0 when their denominator is 0.
 *
 * @param clickThroughRate clicks / impressions.
 * @param startRate starts / impressions.
 * @param completionRate completions / starts.
 * @param returnRate returning users / users; a returning user was active on more than one day.
 */
public record VariantMetricsControllerDto(
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
