-- Raw, append-only user events. Metrics are computed from them, never stored as counters only.
-- user_id and material_id are text: imported history uses the ids of the case data.
CREATE TABLE event(
    id                UUID        PRIMARY KEY,
    user_id           VARCHAR(64) NOT NULL,
    session_id        VARCHAR(64),
    type              VARCHAR(16) NOT NULL,
    material_id       VARCHAR(64) NOT NULL,
    recommendation_id UUID,
    variant           VARCHAR(32),
    position          INTEGER,
    surface           VARCHAR(32),
    occurred_at       TIMESTAMPTZ NOT NULL,
    source            VARCHAR(16) NOT NULL,
    received_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX event_user_occurred_idx ON event(user_id, occurred_at DESC);
CREATE INDEX event_variant_idx ON event(variant) WHERE variant IS NOT NULL;
CREATE INDEX event_material_idx ON event(material_id);
