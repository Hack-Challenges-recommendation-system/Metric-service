package team.nofantasy.recommendationsystem.metricservice.repository;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import team.nofantasy.recommendationsystem.metricservice.entity.Event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    interface VariantCounts {
        String getVariant();

        Long getImpressions();

        Long getClicks();

        Long getStarts();

        Long getCompletions();

        Long getUsers();

        Long getReturningUsers();
    }

    interface MaterialUsers {
        String getMaterialId();

        Long getUsers();
    }

    List<Event> findByUserIdOrderByOccurredAtDesc(String userId, Limit limit);

    boolean existsBySource(Event.EventSource source);

    /**
     * Event counts per A/B variant in {@code [from, to)}. A returning user was active on more than one day.
     */
    @Query(value = """
            WITH scoped AS (
                SELECT * FROM event
                WHERE variant IS NOT NULL AND occurred_at >= :from AND occurred_at < :to
            ),
            per_user AS (
                SELECT variant, user_id, count(DISTINCT date_trunc('day', occurred_at)) AS active_days
                FROM scoped
                GROUP BY variant, user_id
            )
            SELECT s.variant AS "variant",
                   count(*) FILTER (WHERE s.type = 'IMPRESSION') AS "impressions",
                   count(*) FILTER (WHERE s.type = 'CLICK') AS "clicks",
                   count(*) FILTER (WHERE s.type = 'START') AS "starts",
                   count(*) FILTER (WHERE s.type = 'COMPLETE') AS "completions",
                   count(DISTINCT s.user_id) AS "users",
                   (SELECT count(*) FROM per_user p WHERE p.variant = s.variant AND p.active_days > 1) AS "returningUsers"
            FROM scoped s
            GROUP BY s.variant
            ORDER BY s.variant
            """, nativeQuery = true)
    List<VariantCounts> countByVariant(@Param("from") Instant from, @Param("to") Instant to);

    /**
     * Materials that the most distinct users started or completed.
     */
    @Query(value = """
            SELECT material_id AS "materialId", count(DISTINCT user_id) AS "users"
            FROM event
            WHERE type IN ('START', 'COMPLETE')
            GROUP BY material_id
            ORDER BY "users" DESC, material_id
            LIMIT :limit
            """, nativeQuery = true)
    List<MaterialUsers> findPopular(@Param("limit") int limit);

    /**
     * Materials that users started or completed after this one, by number of distinct users.
     */
    @Query(value = """
            SELECT b.material_id AS "materialId", count(DISTINCT b.user_id) AS "users"
            FROM event a
            JOIN event b
              ON b.user_id = a.user_id
             AND b.occurred_at > a.occurred_at
             AND b.material_id <> a.material_id
            WHERE a.material_id = :materialId
              AND a.type IN ('START', 'COMPLETE')
              AND b.type IN ('START', 'COMPLETE')
            GROUP BY b.material_id
            ORDER BY "users" DESC, b.material_id
            LIMIT :limit
            """, nativeQuery = true)
    List<MaterialUsers> findTakenNext(@Param("materialId") String materialId, @Param("limit") int limit);
}
