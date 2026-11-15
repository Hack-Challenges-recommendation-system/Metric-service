package team.nofantasy.recommendationsystem.metricservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Something a user did with a material.
 */
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "event")
public class Event {
    public enum EventType {
        /**
         * The material was shown in a list of recommendations.
         */
        IMPRESSION,
        CLICK,
        START,
        COMPLETE,
        LIKE,
        DISLIKE
    }

    public enum EventSource {
        /**
         * Sent by the frontend.
         */
        APP,
        /**
         * Imported from the interaction log of the case data.
         */
        IMPORT,
        /**
         * Generated for testing; the case allows synthetic events.
         */
        SYNTHETIC
    }

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = 64)
    private String userId;

    @Size(max = 64)
    private String sessionId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EventType type;

    @NotBlank
    @Size(max = 64)
    private String materialId;

    /**
     * The list of recommendations this event belongs to.
     */
    private UUID recommendationId;

    /**
     * A/B variant of that list.
     */
    @Size(max = 32)
    private String variant;

    /**
     * Position of the material in that list, starting at 0.
     */
    private Integer position;

    /**
     * Screen where it happened, e.g. {@code home}.
     */
    @Size(max = 32)
    private String surface;

    @NotNull
    private Instant occurredAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EventSource source;

    @Setter(AccessLevel.NONE)
    @CreationTimestamp
    private Instant receivedAt;
}
