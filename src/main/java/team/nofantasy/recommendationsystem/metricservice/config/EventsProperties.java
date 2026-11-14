package team.nofantasy.recommendationsystem.metricservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param importPath location of the historical events imported at startup, e.g. {@code file:/data/events.json};
 *                   empty skips the import. The case data is mounted at runtime and never committed.
 */
@ConfigurationProperties("app.events")
public record EventsProperties(
        String importPath
) {
}
