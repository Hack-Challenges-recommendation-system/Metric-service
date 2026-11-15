package team.nofantasy.recommendationsystem.metricservice.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import team.nofantasy.recommendationsystem.metricservice.configuration.properties.EventsProperties;

@Configuration
@EnableConfigurationProperties(EventsProperties.class)
public class EventsConfiguration {
}
