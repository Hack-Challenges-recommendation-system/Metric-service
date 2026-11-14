package team.nofantasy.recommendationsystem.metricservice.dto.controller.metric;

/**
 * @param users distinct users who started or completed the material.
 */
public record MaterialUsersControllerDto(
        String materialId,
        long users
) {
}
