# Extension Point Contracts

## Purpose

This document captures the planned public integration contracts that consuming applications may override without changing core task lifecycle behavior.

## 1. Authorization policy

**Planned contract**: `TaskAuthorizationPolicy` in `stem-core`

**Responsibility**:
- Decide whether an actor may perform a lifecycle action on a `UserTask`.
- Evaluate persisted `TaskUser` relationships and task state.
- Return explicit allow/deny outcomes with rejection reasons.

**Default behavior**:
- `OWNER` may act on its assigned task according to lifecycle rules.
- `POTENTIAL_OWNER` may claim/receive assignment according to the configured assignment policy.
- `ADMINISTRATOR` may manage tasks according to the documented override policy.
- Unknown or unresolved actors are denied.

**Override expectations**:
- Consumers may replace the default bean with `@ConditionalOnMissingBean` support from the starter.
- Custom implementations must remain fail-closed and must not bypass lifecycle validation.

## 2. Actor context resolver

**Planned contract**: `ActorContextResolver` (name may vary slightly during implementation)

**Responsibility**:
- Translate application-specific authentication context into the task engine’s actor identifier and optional role hints.
- Support REST adapters and other integration entry points without coupling `stem-core` to a particular security framework.

**Override expectations**:
- Starter and API wiring must use this abstraction when mapping incoming requests.
- Consumers may provide their own resolver to integrate Spring Security, JWT claims, SSO identities, or proprietary identity models.

## 3. Lifecycle event publisher

**Planned contract**: `TaskLifecycleEventPublisher` in `stem-core`

**Responsibility**:
- Publish durable outbox payloads to the downstream system.
- Report success/failure back to the outbox processing workflow.

**Default behavior**:
- The starter provides an Azure Event Hubs implementation when Event Hubs support is explicitly enabled.

**Override expectations**:
- Consumers may provide a custom publisher bean instead of using the Event Hubs integration.
- Custom publishers must preserve at-least-once delivery expectations and expose failure information for retry handling.

## 4. Refusal/lifecycle policy customization

**Planned contract**: lifecycle rule extension in `stem-core`

**Responsibility**:
- Allow documented customization of certain lifecycle details, especially refusal handling, while preserving invariant checks and valid transition ordering.

**Default behavior**:
- `refuse` from `ASSIGNED` records history, clears ownership, and returns the task to `CREATED`.

**Override expectations**:
- Any override must still reject invalid transitions and must keep event/history persistence intact.
- Overrides belong in `stem-core` policy abstractions and are wired by the starter through `@ConditionalOnMissingBean`.

## 5. Starter property gates

**Planned configuration contracts**:
- `stem.task-engine.enabled`
- `stem.task-engine.api.enabled`
- `stem.task-engine.eventhub.enabled`
- `stem.task-engine.lock.timeout`
- `stem.task-engine.outbox.poll-interval`
- `stem.task-engine.outbox.batch-size`

**Contract expectations**:
- All properties remain additive and namespaced under `stem.*`.
- Defaults must keep the feature disabled/inert unless explicitly enabled.
- Configuration metadata should be generated in the starter for discoverability.

## Compatibility notes

- These extension points are part of the planned public integration surface and should be documented and tested as compatibility-sensitive contracts.
- Any later signature or behavior change must follow the constitution’s deprecation and migration expectations.

