# Data Model: Human Task Engine

## Overview

The primary design centers on three user-requested entities:
- `Correlation`: the required parent business context
- `UserTask`: the human work item tracked through its lifecycle
- `TaskUser`: the persisted relationship between a task and an actor/role

Supporting persistence objects are also required for durability and observability:
- `UserTaskLifecycleHistory`
- `UserTaskOutboxEvent`

## Entity Relationship Summary

```text
Correlation 1 --- * UserTask 1 --- * TaskUser
                     |
                     + --- * UserTaskLifecycleHistory
                     |
                     + --- * UserTaskOutboxEvent
```

## 1. Correlation

**Purpose**: Parent business context that must exist before any `UserTask` can be created.

| Field | Type | Required | Notes |
|---|---|---:|---|
| `id` | UUID | yes | Internal primary key |
| `reference` | String | yes | Business reference; combined uniqueness scope with `type` |
| `entity` | String | yes | Human-readable business entity label such as client/company/case |
| `type` | String / enum-like code | yes | Correlation category |
| `createdAt` | Instant | yes | Audit timestamp |
| `createdBy` | String | no | Optional actor identifier |
| `updatedAt` | Instant | yes | Audit timestamp |

**Relationships**:
- One `Correlation` owns zero or more `UserTask` records.

**Validation rules**:
- `reference`, `entity`, and `type` are mandatory.
- A `UserTask` creation request referencing a non-existent `Correlation` is rejected.
- Recommended unique constraint: `uk_correlation_reference_type(reference, type)`.

## 2. UserTask

**Purpose**: Durable human work item associated with exactly one `Correlation`.

| Field | Type | Required | Notes |
|---|---|---:|---|
| `id` | UUID | yes | Internal primary key |
| `correlationId` | UUID | yes | FK to `Correlation` |
| `taskReference` | String | yes | Public/business task reference |
| `title` | String | yes | Short task label |
| `description` | String | no | Optional business detail |
| `state` | `UserTaskState` | yes | Current lifecycle state |
| `ownerUserId` | String | no | Current owner; nullable until assignment |
| `lockVersion` | long | yes | Optimistic concurrency/version column |
| `createdAt` | Instant | yes | Audit timestamp |
| `createdBy` | String | no | Optional creator identifier |
| `updatedAt` | Instant | yes | Audit timestamp |
| `startedAt` | Instant | no | Set when task starts |
| `completedAt` | Instant | no | Set when task completes |
| `completionOutcome` | String | no | Optional outcome/reason summary |
| `lastRefusedAt` | Instant | no | Tracks latest refusal event |

**Relationships**:
- Each `UserTask` belongs to exactly one `Correlation`.
- Each `UserTask` has zero or more `TaskUser` records.
- Each `UserTask` has zero or more lifecycle history entries.
- Each committed state change should create one outbox event row.

**Validation rules**:
- `correlationId`, `taskReference`, `title`, and `state` are mandatory.
- `state` must follow the allowed lifecycle transitions below.
- `ownerUserId` must match a valid `TaskUser` relationship when the task is in `ASSIGNED` or `IN_PROGRESS` unless an administrator override explicitly reassigns ownership.
- Recommended unique constraint: `uk_user_task_reference(taskReference)`.

### UserTask lifecycle states

| State | Meaning |
|---|---|
| `CREATED` | Task exists and is available for assignment or ownership claim |
| `ASSIGNED` | Task has a current owner and may be started or refused |
| `IN_PROGRESS` | Owner has started work |
| `COMPLETED` | Work item is terminal and immutable for lifecycle mutation |

### UserTask lifecycle transitions

| Action | From state(s) | To state | Persistence side effects |
|---|---|---|---|
| `create` | n/a | `CREATED` | Insert `UserTask`, history row, outbox row |
| `assign` | `CREATED` | `ASSIGNED` | Set `ownerUserId`, update version, history row, outbox row |
| `refuse` | `ASSIGNED` | `CREATED` | Clear `ownerUserId`, record refusal metadata, history row, outbox row |
| `start` | `ASSIGNED` | `IN_PROGRESS` | Set `startedAt`, update version, history row, outbox row |
| `complete` | `IN_PROGRESS` | `COMPLETED` | Set `completedAt`, `completionOutcome`, history row, outbox row |

**Rules**:
- Out-of-order transitions are rejected.
- Once `COMPLETED`, no further lifecycle mutation is allowed.
- `refuse` is modeled as a reversible lifecycle action that returns the task to `CREATED`; this default is intentionally overrideable through a documented core policy extension point.

## 3. TaskUser

**Purpose**: Persistent authorization and participation mapping between a `UserTask` and an actor.

| Field | Type | Required | Notes |
|---|---|---:|---|
| `id` | UUID | yes | Internal primary key |
| `userTaskId` | UUID | yes | FK to `UserTask` |
| `userId` | String | yes | Consuming application’s actor identifier |
| `role` | `TaskUserRole` | yes | `OWNER`, `POTENTIAL_OWNER`, or `ADMINISTRATOR` |
| `active` | boolean | yes | Soft-active participation flag |
| `createdAt` | Instant | yes | Audit timestamp |
| `createdBy` | String | no | Optional actor identifier |
| `updatedAt` | Instant | yes | Audit timestamp |

**Relationships**:
- Many `TaskUser` rows may exist for one `UserTask`.
- A single business user may appear in multiple rows across tasks and roles.

**Validation rules**:
- `userTaskId`, `userId`, and `role` are mandatory.
- Recommended uniqueness: one active row per (`userTaskId`, `userId`, `role`).
- `OWNER` is typically single-valued per task at a time; enforce via application rule or filtered unique index if implementation chooses.

**Authorization responsibilities**:
- `OWNER`: can operate the currently assigned task according to lifecycle rules.
- `POTENTIAL_OWNER`: can claim or be assigned the task when allowed by policy.
- `ADMINISTRATOR`: can manage or override assignment/lifecycle operations according to configured policy.

## 4. UserTaskLifecycleHistory (supporting entity)

**Purpose**: Immutable audit log of all accepted lifecycle actions and rejected attempts worth persisting.

| Field | Type | Required | Notes |
|---|---|---:|---|
| `id` | UUID | yes | Primary key |
| `userTaskId` | UUID | yes | FK to `UserTask` |
| `correlationId` | UUID | yes | Denormalized for query convenience |
| `action` | `UserTaskAction` | yes | `CREATE`, `ASSIGN`, `REFUSE`, `START`, `COMPLETE` |
| `fromState` | `UserTaskState` | no | Null for create |
| `toState` | `UserTaskState` | yes | Result state |
| `actorUserId` | String | yes | Acting party |
| `reason` | String | no | Optional refusal/completion/admin reason |
| `occurredAt` | Instant | yes | Event time |
| `metadataJson` | JSON/text | no | Optional extensibility payload |

## 5. UserTaskOutboxEvent (supporting entity)

**Purpose**: Durable outbound publication record for eventual Azure Event Hubs delivery.

| Field | Type | Required | Notes |
|---|---|---:|---|
| `id` | UUID | yes | Outbox event identifier |
| `userTaskId` | UUID | yes | FK to `UserTask` |
| `correlationId` | UUID | yes | Related `Correlation` |
| `eventType` | String | yes | e.g. `stem.task.lifecycle.changed.v1` |
| `payloadJson` | JSON/text | yes | Serialized outbound event payload |
| `status` | enum | yes | `PENDING`, `PUBLISHED`, `FAILED` |
| `retryCount` | int | yes | Retry counter |
| `availableAt` | Instant | yes | Next attempt time |
| `publishedAt` | Instant | no | Set on success |
| `lastError` | String | no | Sanitized failure summary |
| `createdAt` | Instant | yes | Audit timestamp |

## Concurrency and persistence concerns

- `UserTask.lockVersion` is the optimistic concurrency anchor.
- Mutation paths acquire a DB-backed write lock on the `UserTask` row before transition validation and update.
- Azure SQL/SQL Server lock semantics should be validated with SQL Server integration tests rather than relying on H2 substitutes.
- `TaskUser` updates that affect ownership should occur in the same transaction as the parent `UserTask` update.
- Outbox rows must be written in the same transaction as the lifecycle update to avoid missed events.

## Recommended enums

### `TaskUserRole`
- `OWNER`
- `POTENTIAL_OWNER`
- `ADMINISTRATOR`

### `UserTaskState`
- `CREATED`
- `ASSIGNED`
- `IN_PROGRESS`
- `COMPLETED`

### `UserTaskAction`
- `CREATE`
- `ASSIGN`
- `REFUSE`
- `START`
- `COMPLETE`

