# Quickstart: Human Task Engine

## Purpose

This quickstart describes the intended consumer experience for the planned human task engine feature. It reflects the implementation plan and contract artifacts for `001-human-task-engine`.

## Baseline assumptions

- Consumer applications stay on the repository’s existing Spring Boot baseline family; this plan reuses the parent POM Spring Boot version `3.2.5`.
- The task engine is additive and disabled by default.
- Azure SQL is the primary persistence target.
- Downstream task lifecycle notifications are published through either:
  - the provided Azure Event Hubs integration, or
  - a consumer-supplied event publisher bean.

## Planned enablement flow

1. Add the starter dependency to the consuming application.
2. Configure the application datasource for Azure SQL.
3. Opt in to the task engine with `stem.task-engine.enabled=true`.
4. Choose outbound event delivery:
   - enable the built-in Event Hubs path, or
   - provide a custom `TaskLifecycleEventPublisher` bean.
5. Optionally enable REST endpoints with `stem.task-engine.api.enabled=true`.
6. Optionally override default authorization with a custom `TaskAuthorizationPolicy` bean.

## Planned configuration surface

```properties
stem.task-engine.enabled=true
stem.task-engine.api.enabled=false
stem.task-engine.eventhub.enabled=true
stem.task-engine.eventhub.namespace=<event-hubs-namespace>
stem.task-engine.eventhub.hub-name=<event-hub-name>
stem.task-engine.eventhub.connection-string=<connection-string-or-alt-credential>
stem.task-engine.lock.timeout=5s
stem.task-engine.outbox.poll-interval=2s
stem.task-engine.outbox.batch-size=100
```

## Planned programmatic usage flow

### 1. Register a `Correlation`

Create a `Correlation` with:
- `reference`
- `entity`
- `type`

The library rejects `UserTask` creation if the referenced `Correlation` does not already exist.

### 2. Create a `UserTask`

Create a `UserTask` under an existing `Correlation` with:
- task reference
- title
- optional description
- participating `TaskUser` entries for owner/potential owner/administrator roles as needed

Initial state is `CREATED`.

### 3. Perform lifecycle actions

Supported actions:
- `assign`
- `refuse`
- `start`
- `complete`

Default state progression:
- `create` -> `CREATED`
- `assign` -> `ASSIGNED`
- `refuse` -> back to `CREATED`
- `start` -> `IN_PROGRESS`
- `complete` -> `COMPLETED`

Each action returns a success/failure result with the resulting state or a rejection reason.

### 4. Observe downstream events

Each committed lifecycle transition persists an outbox record. The configured publisher sends a lifecycle event to the downstream integration path. Delivery failures remain visible in persisted outbox state and operational signals.

## Planned REST API flow

When `stem.task-engine.api.enabled=true`, the starter wires optional REST adapters from `stem-api`.

Primary planned endpoints:
- `POST /api/stem/correlations`
- `POST /api/stem/tasks`
- `GET /api/stem/tasks/{taskId}`
- `POST /api/stem/tasks/{taskId}/assign`
- `POST /api/stem/tasks/{taskId}/refuse`
- `POST /api/stem/tasks/{taskId}/start`
- `POST /api/stem/tasks/{taskId}/complete`

The detailed request/response contract lives in `contracts/openapi.yaml`.

## Planned extension points

### Authorization

Consumers may provide a custom `TaskAuthorizationPolicy` bean to replace or extend default role checks.

### Actor resolution

API-facing integrations may provide a custom actor-context resolver so authenticated principals are mapped to the library’s actor identifiers without changing `stem-core`.

### Event publishing

Consumers may provide a custom `TaskLifecycleEventPublisher` bean instead of using the built-in Azure Event Hubs publisher.

### Lifecycle policy overrides

The default refusal behavior returns the task to `CREATED`. The plan allows a documented extension point if consumers need different refusal handling while preserving lifecycle invariants.

## Planned validation checklist for implementation

- Verify disabled-by-default startup in a consumer application.
- Verify `Correlation` must exist before `UserTask` creation.
- Verify authorization outcomes for owner, potential owner, administrator, and unauthorized actors.
- Verify only one concurrent mutation commits under multi-runtime contention.
- Verify each committed transition writes history plus outbox data.
- Verify Event Hubs failures do not roll back committed task state.
- Verify consumer overrides win over starter defaults.

## Operational expectations

- Logs and metrics must show lifecycle transitions, authorization failures, lock contention, and outbox delivery outcomes without exposing sensitive business data by default.
- Release notes must state that the feature is opt-in and outline required configuration for Azure SQL and Event Hubs.

