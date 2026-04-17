# Research: Human Task Engine

## Scope

This research resolves the technical choices needed to implement the human task engine described in `C:\Coding\stem\specs\001-human-task-engine\spec.md` while staying aligned with `C:\Coding\stem\.specify\memory\constitution.md`.

## Decision 1: Reuse the existing Spring Boot baseline from the parent POM

**Decision**: Build the feature on the existing repository baseline: Java 17 and Spring Boot `3.2.5` from the root `C:\Coding\stem\pom.xml`.

**Rationale**:
- The constitution requires the Spring Boot 3.x baseline defined in the parent Maven build to remain the supported target unless explicitly amended.
- Reusing the parent BOM avoids accidental version drift across the four modules.
- The feature is additive and does not require a framework upgrade to satisfy the specification.

**Alternatives considered**:
- Upgrading Spring Boot for this feature: rejected because it would broaden compatibility risk and violate the user’s planning input.
- Pinning versions per module: rejected because the constitution requires centralized dependency/version management.

## Decision 2: Keep module placement constitution-aligned

**Decision**: Place shared contracts in `stem-common`, lifecycle/persistence/outbox/authorization orchestration in `stem-core`, optional REST exposure in `stem-api`, and conditional boot wiring plus Event Hub adapter provisioning in `stem-spring-boot-starter`.

**Rationale**:
- This matches the explicit module ownership guidance in the feature spec and constitution.
- It keeps business behavior below transport and starter wiring concerns.
- It leaves the public API surface modular and easier for consumers to adopt selectively.

**Alternatives considered**:
- Putting persistence and lifecycle logic in the starter: rejected because the starter must not own business logic.
- Putting REST DTOs or controllers in `stem-core`: rejected because HTTP adapters belong in `stem-api`.
- Adding a fifth module: rejected because the feature spec assumes the existing four-module structure remains sufficient.

## Decision 3: Use Azure SQL-backed relational persistence in `stem-core`

**Decision**: Model persistence around relational tables for `Correlation`, `UserTask`, `TaskUser`, lifecycle history, and an outbox table, implemented in `stem-core` using JPA/Hibernate plus the SQL Server driver.

**Rationale**:
- The feature explicitly targets Azure SQL.
- The primary entities are strongly relational and require durable transactional consistency.
- JPA/Hibernate plus Boot-managed transaction support fits the existing Spring Boot library direction while keeping persistence-facing logic in the owning module.

**Alternatives considered**:
- Spring JDBC only: viable, but rejected for the initial plan because entity relationships, versioning, and transactional mapping are more laborious without clear benefit here.
- NoSQL/event-store persistence: rejected because the spec explicitly expects relational durability and DB-backed concurrency control.
- Putting persistence adapters in the starter: rejected because persistence-facing behavior belongs in `stem-core`.

## Decision 4: Use combined optimistic and pessimistic concurrency control

**Decision**: Use a version column on `UserTask` for optimistic concurrency and a transaction-scoped database write lock when performing lifecycle mutations; for SQL Server/Azure SQL, allow the persistence implementation to use lock hints (`UPDLOCK`, `ROWLOCK`) or equivalent pessimistic write locking with a bounded lock timeout.

**Rationale**:
- The spec requires a database-level locking mechanism across separate runtimes.
- Pure optimistic locking detects stale updates but does not itself serialize competing mutation attempts early enough for the planned behavior.
- Combining row-version checks with DB-backed write locks provides clear contention outcomes and protects against conflicting state transitions.

**Alternatives considered**:
- Optimistic locking only: rejected because the spec explicitly asks for DB-level locking/concurrency control.
- Distributed application locks (Redis/ZooKeeper/etc.): rejected because the requirement is database-backed and Azure SQL is already the shared coordination substrate.
- Table-wide locks: rejected because they would be too coarse and harm concurrency.

## Decision 5: Represent participation and authorization through `TaskUser` plus overrideable policy SPI

**Decision**: Represent task-user relationships in `TaskUser` records with roles `OWNER`, `POTENTIAL_OWNER`, and `ADMINISTRATOR`, and expose a `TaskAuthorizationPolicy` SPI in `stem-core` so consumers can customize decisions while keeping default behavior fail-closed.

**Rationale**:
- The feature spec names the roles explicitly and requires authorization extension points.
- Persisting participation data makes authorization auditable and deterministic.
- A public SPI keeps authorization customizable without leaking a specific security framework into shared contracts.

**Alternatives considered**:
- Hard-coding all authorization in controllers: rejected because programmatic library methods must behave equivalently and authorization belongs below the transport layer.
- Depending directly on Spring Security types in `stem-common`: rejected because `stem-common` must avoid Spring runtime coupling.
- Leaving authorization entirely to consumers: rejected because the feature itself must enforce role-based task behavior.

## Decision 6: Use a transactional outbox for downstream lifecycle events

**Decision**: Persist a lifecycle outbox record in the same transaction as each committed task state change, and publish it asynchronously via a `TaskLifecycleEventPublisher` SPI with an Event Hub implementation supplied by the starter when enabled.

**Rationale**:
- The spec requires committed task state changes to survive temporary downstream outages without silently dropping events.
- An outbox gives durability, retry visibility, and clean separation between core transaction handling and transport-specific event publication.
- The SPI keeps the design override-friendly for consumers who want a different publishing mechanism.

**Alternatives considered**:
- Publishing directly to Event Hub inside the task mutation transaction: rejected because downstream outages would couple external availability to core task commits.
- Fire-and-forget asynchronous publish without persistence: rejected because it risks silent event loss.
- Putting Event Hub transport code in `stem-core`: rejected because transport-specific wiring belongs above core domain behavior.

## Decision 7: Supply Azure Event Hubs integration from the starter, not the core

**Decision**: Keep Event Hubs-specific client dependencies and bean wiring in `stem-spring-boot-starter`, activated only when `stem.task-engine.eventhub.enabled=true` and no consumer override bean exists.

**Rationale**:
- The constitution requires explicit, conditional, override-friendly auto-configuration.
- This keeps `stem-core` independent of Azure transport libraries while still enabling a first-class default integration path.
- Existing consumers remain unaffected unless they opt in.

**Alternatives considered**:
- Making Event Hubs a hard dependency of `stem-core`: rejected because it would over-couple the domain module.
- Auto-enabling Event Hubs whenever the Azure SDK is on the classpath: rejected because hidden side effects are prohibited.

## Decision 8: Replace component scanning with explicit starter auto-configuration

**Decision**: Replace the current `@ComponentScan`-based `StemAutoConfiguration` approach with explicit auto-configuration classes, conditional beans, `@ConfigurationProperties`, and property gates for the engine, API layer, and Event Hub integration.

**Rationale**:
- The constitution explicitly prohibits relying on broad component scanning as the primary integration mechanism.
- Explicit registration is easier to reason about, cheaper at startup, and friendlier to consumer overrides.
- The feature spec explicitly calls for starter behavior that is explicit and override-friendly.

**Alternatives considered**:
- Keeping the existing component scan and adding new packages under it: rejected because it conflicts with the constitution and would make enablement too implicit.
- Enabling all task-engine beans by default: rejected because the feature must remain opt-in.

## Decision 9: Expose both library and optional REST interfaces with equivalent semantics

**Decision**: Define public service contracts in `stem-core` for correlation registration and task lifecycle actions, and document matching optional REST endpoints in `stem-api` for create/query/assign/refuse/start/complete flows.

**Rationale**:
- The spec requires both Java class methods and APIs.
- Equivalent semantics keep behavior consistent across integration styles.
- Separating service contracts from transport adapters preserves testability and backwards compatibility.

**Alternatives considered**:
- REST only: rejected because the spec explicitly requires library methods.
- Library methods only: rejected because the spec explicitly requires APIs.

## Decision 10: Test with reactor-level coverage focused on boundaries

**Decision**: Plan unit tests in owning modules, contract/schema tests for public APIs and events, SQL Server Testcontainers integration tests for persistence and lock contention, and starter application-context tests for property conditions and overrides.

**Rationale**:
- The constitution requires deterministic evidence across unit, contract, integration, and starter-context boundaries when applicable.
- The most failure-prone behaviors here are lifecycle rules, authorization, cross-runtime concurrency, and outbox delivery.
- SQL Server Testcontainers coverage is the most direct way to validate Azure SQL-compatible locking behavior before runtime deployment.

**Alternatives considered**:
- H2-only tests: rejected because SQL Server locking semantics differ materially.
- Manual testing only: rejected because the constitution requires automated verification.

## Resulting implementation posture

All technical context items are resolved for planning purposes. No `NEEDS CLARIFICATION` items remain. The design can proceed with:
- Spring Boot `3.2.5` baseline reuse
- Constitution-aligned module placement
- Azure SQL + JPA/Hibernate persistence in `stem-core`
- DB-backed task locking with optimistic + pessimistic coordination
- `TaskUser`-driven authorization plus overrideable SPI
- Transactional outbox + optional Azure Event Hubs publisher
- Explicit, property-gated starter auto-configuration

