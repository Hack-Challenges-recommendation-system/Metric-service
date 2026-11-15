package team.nofantasy.recommendationsystem.metricservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;
import team.nofantasy.recommendationsystem.metricservice.entity.Event;
import team.nofantasy.recommendationsystem.metricservice.importer.EventImporter;
import team.nofantasy.recommendationsystem.metricservice.repository.EventRepository;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Events and metrics on real Postgres. Each test uses its own ids, so tests don't see each other's data.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class MetricIntegrationTest {
    private static final String DAY_1 = "2026-11-01T10:00:00Z";
    private static final String DAY_2 = "2026-11-02T10:00:00Z";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private EventImporter eventImporter;

    private static MockHttpServletRequestBuilder api(MockHttpServletRequestBuilder builder) {
        return builder.servletPath("/api/v1");
    }

    private static String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private static String event(String type, String materialId, String variant, String occurredAt) {
        return """
                {"type": "%s", "materialId": "%s", "variant": %s, "occurredAt": "%s"}
                """.formatted(type, materialId, variant == null ? "null" : "\"" + variant + "\"", occurredAt);
    }

    private void send(UUID userId, String... events) throws Exception {
        mockMvc.perform(api(post("/api/v1/events"))
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"events\": [" + String.join(",", events) + "]}"))
                .andExpect(status().isAccepted());
    }

    @Test
    void importsHistoryOnceAndSkipsInvalidRows() {
        long imported = eventRepository.findAll().stream()
                .filter(event -> event.getSource() == Event.EventSource.IMPORT)
                .count();
        assertThat(imported).isEqualTo(3);

        eventImporter.importOnStartup();

        assertThat(eventRepository.findAll().stream().filter(event -> event.getSource() == Event.EventSource.IMPORT))
                .hasSize(3);
    }

    @Test
    void returnsOnlyOwnHistoryNewestFirst() throws Exception {
        UUID userId = UUID.randomUUID();
        String material = "h-" + suffix();
        send(userId, event("IMPRESSION", material, null, DAY_1), event("CLICK", material, null, DAY_2));
        send(UUID.randomUUID(), event("CLICK", material, null, DAY_2));

        mockMvc.perform(api(get("/api/v1/events/me")).header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("CLICK"))
                .andExpect(jsonPath("$[1].type").value("IMPRESSION"));
    }

    @Test
    void rejectsInvalidEvents() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(api(post("/api/v1/events")).header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"events\": []}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(api(post("/api/v1/events")).header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"events\": [{\"type\": \"VIEWED\", \"materialId\": \"m\"}]}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(api(post("/api/v1/events")).header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"events\": [{\"type\": \"CLICK\"}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['events[0].materialId']").exists());
        mockMvc.perform(api(post("/api/v1/events"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"events\": [" + event("CLICK", "m", null, DAY_1) + "]}"))
                .andExpect(status().isBadRequest());

        String tooMany = IntStream.range(0, 101).mapToObj(i -> event("CLICK", "m", null, DAY_1)).collect(Collectors.joining(","));
        mockMvc.perform(api(post("/api/v1/events")).header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"events\": [" + tooMany + "]}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(api(get("/api/v1/events/me")).header("X-User-Id", userId).param("limit", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void computesExperimentMetricsPerVariant() throws Exception {
        String variant = "exp-" + suffix();
        String first = "e1-" + suffix();
        String second = "e2-" + suffix();

        send(UUID.randomUUID(),
                event("IMPRESSION", first, variant, DAY_1),
                event("IMPRESSION", second, variant, DAY_1),
                event("CLICK", first, variant, DAY_1),
                event("START", first, variant, DAY_1),
                event("COMPLETE", first, variant, DAY_1));
        send(UUID.randomUUID(),
                event("IMPRESSION", first, variant, DAY_1),
                event("IMPRESSION", second, variant, DAY_1),
                event("IMPRESSION", first, variant, DAY_2));

        String row = "$[?(@.variant == '" + variant + "')]";
        mockMvc.perform(api(get("/api/v1/metrics/experiment")))
                .andExpect(status().isOk())
                .andExpect(jsonPath(row + ".impressions", contains(5)))
                .andExpect(jsonPath(row + ".clicks", contains(1)))
                .andExpect(jsonPath(row + ".users", contains(2)))
                .andExpect(jsonPath(row + ".returningUsers", contains(1)))
                .andExpect(jsonPath(row + ".clickThroughRate", contains(0.2)))
                .andExpect(jsonPath(row + ".startRate", contains(0.2)))
                .andExpect(jsonPath(row + ".completionRate", contains(1.0)))
                .andExpect(jsonPath(row + ".returnRate", contains(0.5)));

        mockMvc.perform(api(get("/api/v1/metrics/experiment")).param("from", DAY_2).param("to", "2026-11-03T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(row + ".impressions", contains(1)))
                .andExpect(jsonPath(row + ".users", contains(1)));
    }

    @Test
    void findsPopularMaterialsAndWhatUsersTakeNext() throws Exception {
        String popular = "p-" + suffix();
        String next = "n-" + suffix();
        String other = "o-" + suffix();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        UUID third = UUID.randomUUID();

        send(first, event("START", popular, null, DAY_1), event("START", next, null, DAY_2));
        send(second, event("START", popular, null, DAY_1), event("COMPLETE", next, null, DAY_2));
        send(third, event("START", popular, null, DAY_1), event("START", other, null, DAY_2));

        mockMvc.perform(api(get("/api/v1/metrics/materials/" + popular + "/next")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].materialId").value(next))
                .andExpect(jsonPath("$[0].users").value(2))
                .andExpect(jsonPath("$[1].materialId").value(other));

        mockMvc.perform(api(get("/api/v1/metrics/materials/popular")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].materialId").value(popular))
                .andExpect(jsonPath("$[0].users").value(3));
    }
}
