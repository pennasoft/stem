# Feature Specification: Human Task Engine

**Feature Branch**: `[001-human-task-engine]`  
**Created**: 2026-04-17  
**Status**: Draft  
**Input**: User description: "Implement a human task engine framework library for other applications to use in their project. The library must support creating, assigning, refusing, starting, and completing tasks through Java class methods and via APIs. Tasks follow a defined lifecycle.

Context and required capabilities:
- This is a multi-module Spring Boot starter/library project.
- Applications using this library will run in containerized environments such as Kubernetes.
- Persistence target is Azure SQL.
- There must be a database-level locking mechanism to prevent different runtimes from changing the same task entry concurrently.
- Tasks are correlated through a parent entity called a correlation.
- A correlation has: reference, entity (such as client/company/case name), and type.
- Tasks cannot be created before their correlation exists.
- There will be user authorization to manage or interact with a task.
- A user can be an owner, potential owner, or administrator of a task.
- When a task state changes, the library must send an event to another application through Azure Event Hub."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Manage correlated task lifecycles (Priority: P1)

As a consuming application team, I want to register correlations and create tasks beneath them, then move those tasks through their allowed lifecycle actions through programmatic library methods or exposed APIs, so that business work can be coordinated consistently across applications.

**Why this priority**: Without a reliable way to create correlated tasks and move them through their lifecycle, the library does not provide usable business value to consuming applications.

**Independent Test**: Can be fully tested by creating a correlation, creating a task against it, and executing the supported lifecycle actions through both interaction styles while verifying the persisted state, authorization outcome, and returned task details.

**Acceptance Scenarios**:

1. **Given** a persisted correlation with a reference, entity, and type, **When** a consuming application creates a task for that correlation, **Then** the task is stored with that correlation and enters its initial lifecycle state.
2. **Given** an existing task in a state that allows assignment, **When** an authorized actor assigns the task through a library method or API, **Then** the task reflects the new assignment and remains in a valid lifecycle state.
3. **Given** an assigned task, **When** an authorized actor starts and then completes the task, **Then** each transition is accepted only in order and the task ends in a completed state.
4. **Given** a task in a state that allows refusal, **When** an authorized actor refuses it, **Then** the refusal is recorded and any follow-up handling required by the lifecycle rules is applied consistently.

---

### User Story 2 - Enforce task participation permissions (Priority: P2)

As a business user or administrative operator, I want task actions to honor my relationship to the task, so that only permitted people can manage or work on task items.

**Why this priority**: The task engine must protect task integrity and business accountability by preventing unauthorized lifecycle changes.

**Independent Test**: Can be fully tested by attempting the same action with an owner, a potential owner, an administrator, and an unauthorized user and verifying that the outcome matches the documented permission rules.

**Acceptance Scenarios**:

1. **Given** a task with defined owner, potential owner, and administrator relationships, **When** each participant attempts an allowed action, **Then** the action succeeds according to their permitted role.
2. **Given** a task and a user with no authorized relationship to it, **When** that user attempts to assign, refuse, start, or complete the task, **Then** the request is rejected without changing task state.
3. **Given** an unassigned task with one or more potential owners, **When** a potential owner takes ownership through a supported action, **Then** the task records the new owner and prevents conflicting ownership changes.

---

### User Story 3 - Maintain consistency across runtimes and notify downstream systems (Priority: P3)

As a platform operator or integrating application, I want concurrent runtimes to avoid conflicting updates to the same task and every committed task state change to be shared with downstream systems, so that task data remains trustworthy in containerized deployments and connected applications stay in sync.

**Why this priority**: Multi-runtime deployments and downstream integrations are core operating constraints for this library, and failures here would create data corruption or missed business processing.

**Independent Test**: Can be fully tested by running competing task updates from separate runtime instances and verifying that only one update commits per contention attempt while every committed state change produces one downstream-consumable lifecycle event.

**Acceptance Scenarios**:

1. **Given** two runtime instances attempt to change the same task at nearly the same time, **When** both submit a valid lifecycle action, **Then** only one change is committed and the other receives a concurrency failure result.
2. **Given** a task lifecycle action is committed successfully, **When** the state change is finalized, **Then** a lifecycle event describing the change is made available to the downstream application.
3. **Given** a downstream event delivery dependency is temporarily unavailable, **When** a task state change occurs, **Then** the library preserves the committed task state, exposes that delivery attention is required, and supports eventual downstream notification without silently dropping the event.

---

### Edge Cases

- A consuming application attempts to create a task for a correlation that does not exist.
- A lifecycle action is requested out of order, repeated after completion, or otherwise invalid for the current task state.
- Two or more runtime instances contend to update the same task at nearly the same time.
- A user attempts an action with insufficient authorization or with stale ownership information.
- A consuming application defines its own service, API adapter, authorization resolver, or event publisher and expects the starter to defer to that override.
- Required persistence or eventing dependencies are missing while the feature is explicitly enabled.
- Existing consumers upgrade the library without enabling the human task engine and expect no startup or behavior regressions.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST allow consuming applications to register and persist a correlation containing a reference, an entity label, and a correlation type before any task is created for that business context.
- **FR-002**: System MUST reject task creation when the referenced correlation does not already exist.
- **FR-003**: System MUST allow consuming applications to create a task linked to an existing correlation through programmatic library methods and through externally exposed APIs with equivalent business behavior.
- **FR-004**: System MUST support the task lifecycle actions create, assign, refuse, start, and complete, and MUST only allow transitions that are valid for the task’s current lifecycle state.
- **FR-005**: System MUST return a clear success or failure result for every lifecycle action, including the resulting task state for successful requests and the rejection reason for unsuccessful requests.
- **FR-006**: System MUST support task participation roles for owner, potential owner, and administrator and MUST evaluate each requested task action against those roles before applying the change.
- **FR-007**: System MUST prevent unauthorized users from managing or interacting with a task and MUST leave persisted task state unchanged when authorization fails.
- **FR-008**: System MUST support assignment flows in which a task can be assigned to an owner, offered to potential owners, refused by an eligible participant, and subsequently moved according to the defined lifecycle rules.
- **FR-009**: System MUST persist correlations, tasks, assignments, and lifecycle history in a form suitable for durable use in the project’s relational persistence environment.
- **FR-010**: System MUST use a database-backed locking or concurrency control mechanism so that separate runtime instances cannot commit conflicting changes to the same task entry concurrently.
- **FR-011**: System MUST behave correctly in containerized multi-runtime deployments by ensuring that concurrent task mutation attempts yield a single committed task outcome.
- **FR-012**: System MUST emit a task lifecycle event whenever a task state change is committed, and the event MUST include enough business context for a downstream application to identify the task, its correlation, the performed transition, and the acting party.
- **FR-013**: System MUST route lifecycle events to the organization’s designated downstream event streaming integration for task state changes.
- **FR-014**: System MUST expose delivery failure visibility for lifecycle events so consuming applications can detect and remediate downstream synchronization problems.
- **FR-015**: System MUST offer configuration and extension points that let consuming applications integrate their own authorization, transport, and event-handling preferences without changing core task lifecycle behavior.

### Module Impact & Compatibility *(mandatory)*

- **Owning Module**: `stem-core` owns the task lifecycle rules, correlation enforcement, persistence-facing behavior, authorization enforcement, locking coordination, and lifecycle event orchestration because it is the lowest stable module that can own domain behavior.
- **Additional Modules Impacted**: `stem-common` for shared lifecycle contracts, role definitions, and event payload models; `stem-api` for external API adapters that expose task operations; `stem-spring-boot-starter` for opt-in integration wiring, configuration metadata, consumer overrides, and starter-specific verification; repository-level documentation for developer guidance.
- **Public Surface Changes**: New consumer-facing contracts for correlations, tasks, lifecycle actions, authorization roles, operation results, optional API endpoints, configuration properties under the `stem.*` namespace, and a documented outbound lifecycle event contract.
- **Compatibility Plan**: This feature is additive and must remain opt-in for existing consumers. Applications that do not enable the task engine or its APIs must retain current startup behavior and current documented behavior. Any later change to lifecycle semantics, payload shapes, or configuration defaults must follow documented deprecation and migration guidance before removal.
- **Auto-Configuration Impact**: The starter may introduce new conditional beans, configuration properties, and metadata to wire the task engine, persistence support, authorization integration, API adapters, and outbound eventing. All auto-configuration must remain explicit, cheap at startup, override-friendly, and disabled or inert when required prerequisites are absent.

### Operational, Security, and Documentation Impact *(mandatory)*

- **Observability**: Add actionable signals for lifecycle transitions, authorization rejections, lock-contention outcomes, and outbound event delivery status using logs, metrics, or traces that help operators diagnose business-impacting issues without emitting sensitive task data by default.
- **Security Considerations**: Treat task actions as fail-closed operations; validate all correlation, task, and actor inputs; enforce least-privilege role checks; document how consuming applications provide authentication and authorization context; prevent unauthorized state changes; and avoid exposing secrets or unnecessary business-identifying data in logs or events.
- **Documentation Updates**: Update the root `README.md`, relevant module-level READMEs, configuration reference material, lifecycle and role behavior documentation, API usage examples, event contract documentation, and release notes for the new consumer-visible capability.

### Constraints & Constitution Alignment

- **Module Ownership Discipline**: Shared contracts and neutral types belong in `stem-common`; domain behavior and persistence coordination belong in `stem-core`; externally exposed transport adapters belong in `stem-api`; and `stem-spring-boot-starter` must only wire and conditionally expose the feature, not own business rules.
- **Dependency Hygiene**: Any new dependency required for persistence, authorization, API exposure, or eventing must be justified, centrally managed from the root `pom.xml`, and scoped to the smallest affected module. `stem-common` must remain free of avoidable Spring, web, or persistence coupling.
- **Testing Expectations**: Delivery must include unit tests in the owning modules, contract coverage for public lifecycle and event contracts, integration coverage for persistence and cross-runtime locking behavior, and starter context tests for any new auto-configuration or configuration properties.
- **Auto-Configuration Discipline**: Bean registration must be conditional and override-friendly, must not rely on broad component scanning as the primary integration mechanism, and must not expose external APIs or create external connections unless explicitly enabled and documented.
- **Security and Privacy Discipline**: Secure defaults, input validation, least-privilege assumptions, and downstream event/data minimization are mandatory, especially for role-based task access and business correlation data.
- **Documentation Discipline**: Every externally visible contract, configuration key, extension point, and upgrade implication must be documented before release so consuming teams can adopt the feature safely.

### Key Entities *(include if feature involves data)*

- **Correlation**: The parent business context for a group of tasks, identified by a reference, an entity label, and a type. A correlation must exist before any child task can be created.
- **Task**: A human work item associated with one correlation, carrying its current lifecycle state, assignment and participation data, history of state changes, and completion outcome.
- **Task Participant**: A user relationship to a task that determines permissions, including owner, potential owner, and administrator.
- **Task Lifecycle Event**: A downstream notification representing a committed task state transition together with identifying business context needed by external consumers.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: In acceptance testing, a consuming application team can register a correlation, create a task, assign it, start it, and complete it through documented entry points in under 30 minutes without relying on undocumented library behavior.
- **SC-002**: In validation scenarios, 100% of task-creation attempts without an existing correlation are rejected with a clear reason and no task record is created.
- **SC-003**: In role-based acceptance scenarios, 100% of unauthorized lifecycle action attempts are rejected without changing persisted task state, while authorized role-based actions succeed according to the documented permission model.
- **SC-004**: In repeated concurrency validation with at least two runtime instances contending for the same task, every contention attempt results in one committed outcome and zero conflicting persisted task states.
- **SC-005**: In downstream integration testing, 100% of committed task state changes produce a downstream-consumable lifecycle event within 5 seconds under normal operating conditions, and delivery exceptions are surfaced for operator action.
- **SC-006**: Existing consumers that upgrade without enabling the human task engine experience no new required configuration and no startup failure in upgrade smoke tests.

## Assumptions

- Consuming applications run on the repository’s supported baseline and can supply the user identity and authorization context needed for role-based task decisions.
- The project continues to use its existing four-module structure, and this feature can be delivered without introducing a new Maven module.
- The primary persistence environment provides the transactional guarantees needed to enforce durable task state changes and cross-runtime concurrency control.
- Downstream applications that receive lifecycle events can identify duplicate deliveries safely if retries are needed.
- Existing consumers keep current behavior unless they explicitly enable the new task engine capability or integrate its optional APIs.
- Any new third-party dependency introduced for this feature will be reviewed for security and centrally managed from the root build before implementation proceeds.

