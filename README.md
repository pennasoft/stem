# stem

STEM is a multi-module Maven project for a Spring Boot-friendly task engine library.

## Module layout

- `stem-common`: shared contracts, DTOs, enums, and utility types
- `stem-core`: domain logic, lifecycle handling, and persistence-facing behavior
- `stem-api`: API adapters for externally exposed transport concerns
- `stem-spring-boot-starter`: opt-in Spring Boot auto-configuration for consuming apps

## Human task engine feature

The repository now includes an additive human task engine implementation with:

- correlation registration before task creation
- task lifecycle operations for create, assign, refuse, start, and complete
- persisted task participants with `OWNER`, `POTENTIAL_OWNER`, and `ADMINISTRATOR` roles
- lifecycle history and transactional outbox records for every committed state change
- optional REST endpoints under `/api/stem/**`
- opt-in starter wiring under the `stem.task-engine.*` namespace

### Starter enablement

Typical consumer properties:

```properties
stem.task-engine.enabled=true
stem.task-engine.api.enabled=true
stem.task-engine.eventhub.enabled=false
stem.task-engine.outbox.poll-interval=2s
stem.task-engine.outbox.batch-size=100
```

When the engine is enabled, the consuming application must provide either:

- a custom `TaskLifecycleEventPublisher` bean, or
- Event Hubs configuration with `stem.task-engine.eventhub.enabled=true`

The feature remains disabled by default, so existing consumers should continue to start unchanged until they opt in.

## Development expectations

- Start changes in the lowest viable module and avoid leaking business logic into the
  starter or API adapters.
- Preserve backwards compatibility for published APIs, configuration properties, and
  documented runtime behavior.
- Keep auto-configuration explicit, conditional, and easy for consumers to override.
- Add tests for every change, including starter context tests when Boot wiring changes.
- Centralize dependency and version management in the root `pom.xml`.

## Working agreement

Repository governance is defined in `C:\Coding\stem\.specify\memory\constitution.md`.
Planning templates under `.specify/templates/` are aligned to that constitution and should
be used for feature planning, specification, and task generation.
