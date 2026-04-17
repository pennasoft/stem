---

description: "Task list template for feature implementation"
---

# Tasks: [FEATURE NAME]

**Input**: Design documents from `/specs/[###-feature-name]/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Tests are REQUIRED. Every user story and every public-surface change MUST include the appropriate unit, integration, contract, and/or starter-context coverage required by the constitution.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Shared contracts/utilities**: `stem-common/src/main/java`, `stem-common/src/test/java`
- **Domain logic/persistence**: `stem-core/src/main/java`, `stem-core/src/test/java`
- **HTTP/API adapters**: `stem-api/src/main/java`, `stem-api/src/test/java`
- **Starter wiring**: `stem-spring-boot-starter/src/main/java`, `stem-spring-boot-starter/src/test/java`, `stem-spring-boot-starter/src/main/resources/META-INF/spring`
- Paths shown below assume this repository layout - adjust task descriptions to the impacted modules from `plan.md`

<!-- 
  ============================================================================
  IMPORTANT: The tasks below are SAMPLE TASKS for illustration purposes only.
  
  The /speckit.tasks command MUST replace these with actual tasks based on:
  - User stories from spec.md (with their priorities P1, P2, P3...)
  - Feature requirements from plan.md
  - Entities from data-model.md
  - Endpoints from contracts/
  
  Tasks MUST be organized by user story so each story can be:
  - Implemented independently
  - Tested independently
  - Delivered as an MVP increment
  
  DO NOT keep these sample tasks in the generated tasks.md file.
  ============================================================================
-->

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [ ] T001 Confirm impacted module boundaries per implementation plan
- [ ] T002 Add or update dependencies in the root `pom.xml` and affected module `pom.xml` files
- [ ] T003 [P] Scaffold tests and documentation placeholders in the impacted modules

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

Examples of foundational tasks (adjust based on your project):

- [ ] T004 Establish shared contracts or domain abstractions in the lowest viable module
- [ ] T005 [P] Add compatibility guards such as deprecations, adapter layers, or migration shims
- [ ] T006 [P] Prepare starter conditions, configuration properties, or explicit bean registration where required
- [ ] T007 Create or update baseline test fixtures for module, integration, or context-runner coverage
- [ ] T008 Configure logging, metrics, or tracing hooks needed for the feature
- [ ] T009 Document security assumptions, input validation, and dependency impact before story work begins

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - [Title] (Priority: P1) 🎯 MVP

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 1 (REQUIRED) ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T010 [P] US1 Add unit test coverage in the owning module test source set
- [ ] T011 [P] US1 Add integration, contract, or starter-context test coverage for the impacted boundary

### Implementation for User Story 1

- [ ] T012 [P] US1 Implement shared contracts or models in the selected module path
- [ ] T013 [P] US1 Implement supporting services, mappers, or configuration properties in the owning module
- [ ] T014 US1 Implement feature behavior in the owning module after tests fail
- [ ] T015 US1 Wire higher-level adapters or starter beans only if required by the plan
- [ ] T016 US1 Add validation, compatibility handling, and secure defaults
- [ ] T017 US1 Add or refine logs, metrics, traces, and documentation for user story 1

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - [Title] (Priority: P2)

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 2 (REQUIRED) ⚠️

- [ ] T018 [P] US2 Add unit test coverage in the owning module test source set
- [ ] T019 [P] US2 Add integration, contract, or starter-context test coverage for the impacted boundary

### Implementation for User Story 2

- [ ] T020 [P] US2 Implement shared contracts, services, or configuration changes in the selected module path
- [ ] T021 US2 Implement behavior and compatibility handling after tests fail
- [ ] T022 US2 Wire higher-level adapters or starter beans only if required by the plan
- [ ] T023 US2 Update observability, security, and documentation for user story 2

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - [Title] (Priority: P3)

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 3 (REQUIRED) ⚠️

- [ ] T024 [P] US3 Add unit test coverage in the owning module test source set
- [ ] T025 [P] US3 Add integration, contract, or starter-context test coverage for the impacted boundary

### Implementation for User Story 3

- [ ] T026 [P] US3 Implement shared contracts, services, or configuration changes in the selected module path
- [ ] T027 US3 Implement behavior and compatibility handling after tests fail
- [ ] T028 US3 Wire higher-level adapters or starter beans only if required by the plan

**Checkpoint**: All user stories should now be independently functional

---

[Add more user story phases as needed, following the same pattern]

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] TXXX [P] Documentation updates in `README.md`, module docs, and release notes
- [ ] TXXX Code cleanup and refactoring
- [ ] TXXX Performance and startup-cost review across all stories
- [ ] TXXX [P] Additional compatibility, integration, and starter-context tests where needed
- [ ] TXXX Security hardening
- [ ] TXXX Validate Maven reactor build and consumer-style starter wiring

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - May integrate with US1 but should be independently testable
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - May integrate with US1/US2 but should be independently testable

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- Lowest-level module changes before higher-level adapters or starter wiring
- Shared contracts before services
- Services before API or starter registration
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, all user stories can start in parallel (if team capacity allows)
- All tests for a user story marked [P] can run in parallel
- Independent module changes within a story marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members

---

## Parallel Example: User Story 1

```bash
# Launch all required tests for User Story 1 together:
Task: "Add unit test coverage in the owning module test source set"
Task: "Add integration, contract, or starter-context test coverage for the impacted boundary"

# Launch independent implementation work for User Story 1 together:
Task: "Implement shared contracts or models in the selected module path"
Task: "Implement supporting services, mappers, or configuration properties in the owning module"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo
5. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1
   - Developer B: User Story 2
   - Developer C: User Story 3
3. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify required tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, undocumented compatibility risk, and starter changes without explicit conditions or docs
