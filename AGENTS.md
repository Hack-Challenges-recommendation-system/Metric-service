# metric-service

Stores what users do with materials and computes metrics from it: the history of each user, A/B metrics per variant and popularity signals for recommendations.

**Read the shared guide first:** [AGENTS.md in Documentation](https://github.com/Hack-Challenges-recommendation-system/Documentation/blob/develop/AGENTS.md) covers the project, its architecture, the rules from the case (the case data and keys never go into git; this repository is public), the commit conventions and the conventions of the Java services. Locally it is `AGENTS.md` in your clone of the Documentation repository. This file adds only what is specific to metric-service.

## How events work

- Events are append-only rows. Metrics are computed with SQL on request, never kept only as counters.
- `user_id` and `material_id` are text, because imported history uses the ids of the case data. App events store the UUID from `X-User-Id`.
- `source` is `APP` (frontend), `IMPORT` (case data) or `SYNTHETIC` (generated test data, which the case allows).
- At startup `EventImporter` loads `EVENTS_IMPORT_PATH` once, a JSON array of `EventImportDto` (`userId`, `type`, `materialId`, `occurredAt`, `sessionId`). If imported events already exist, it skips. As with the catalog, the layout of the case data isn't mapped yet.

## Event types

`IMPRESSION` (shown in a list of recommendations), `CLICK`, `START`, `COMPLETE`, `LIKE`, `DISLIKE`.
For events from a list of recommendations, also send `recommendationId`, `variant` (A/B) and `position` (from 0). `surface` names the screen, e.g. `home`.

## Endpoints (under `/api/v1`)

| Method and path | Caller | What it does |
|---|---|---|
| `POST /events` | signed in | `{events: [...]}` with 1–100 events → 202; `occurredAt` defaults to server time |
| `GET /events/me?limit=` | signed in | Newest first; `limit` 1–500, default 50 |
| `GET /metrics/experiment?from=&to=` | public | Per variant: impressions, clicks, starts, completions, users, returning users and rates. `from` inclusive, `to` exclusive, ISO-8601 |
| `GET /metrics/materials/popular?limit=` | public | Materials that the most distinct users started or completed |
| `GET /metrics/materials/{id}/next?limit=` | public | What users started or completed after this material |
| `GET /health` | anyone | 200 |

Rates: click-through = clicks / impressions, start rate = starts / impressions, completion rate = completions / starts, return rate = users active on more than one day / users. A rate is 0 when its denominator is 0. Metrics endpoints return aggregates only, never user ids.

## Configuration

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5438/postgres` |
| `DB_USERNAME`, `DB_PASSWORD` | `admin`, `admin` |
| `EVENTS_IMPORT_PATH` | empty: no import |

`docker compose up -d` starts Postgres on `127.0.0.1:5438`.

## Tests

- `MetricIntegrationTest` on real Postgres: one-time import, history isolation between users, validation, experiment metrics from known events, popular and next materials.
- `MetricServiceTest`: rate calculation, including zero denominators.
