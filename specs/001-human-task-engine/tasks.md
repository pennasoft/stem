# Tasks: Human Task Engine

## Phase 1 - Setup

- [X] Centralize new dependencies and module wiring in the Maven reactor.
- [X] Replace starter component scanning with explicit auto-configuration imports.
- [X] Verify repository ignore coverage for Java and universal generated files.

## Phase 2 - Shared Contracts

- [X] Add task lifecycle enums, participant roles, commands, views, operation results, and lifecycle event payloads in `stem-common`.

## Phase 3 - Core Engine

- [X] Implement correlation registration and user task service contracts in `stem-core`.
- [X] Add JPA persistence for correlations, tasks, participants, lifecycle history, and outbox records.
- [X] Enforce default authorization and lifecycle rules for assign, refuse, start, and complete transitions.
- [X] Serialize lifecycle events into a transactional outbox for asynchronous publication.

## Phase 4 - API Adapters

- [X] Add validated REST request models for correlation and task operations in `stem-api`.
- [X] Implement optional REST controllers and exception/status mapping aligned to the planned contract.
- [X] Add the actor context resolver extension point for API integrations.

## Phase 5 - Starter Integration

- [X] Add `stem.task-engine.*` configuration properties.
- [X] Wire core services, optional API controllers, outbox scheduling, and Event Hubs publisher support through conditional starter beans.
- [X] Keep the engine disabled by default and fail fast when enabled without an event publisher.

## Phase 6 - Validation and Documentation

- [X] Add core integration tests for lifecycle flow, authorization rejection, and missing-correlation rejection.
- [X] Add API controller tests for create/get/action status mapping.
- [X] Add starter context tests for disabled-by-default and enabled wiring scenarios.
- [X] Update the root `README.md` with feature and configuration guidance.

