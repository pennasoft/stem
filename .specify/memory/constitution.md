<!--
Sync Impact Report
Version change: template -> 1.0.0
Modified principles:
- Template principle 1 -> I. Library-First Module Boundaries
- Template principle 2 -> II. Backwards Compatibility Is the Default
- Template principle 3 -> III. Auto-Configuration Must Be Explicit and Override-Friendly
- Template principle 4 -> IV. Testing and Verification Are Non-Negotiable
- Template principle 5 -> V. Dependency, Observability, Security, and Documentation Hygiene
Added sections:
- Engineering Standards
- Delivery Workflow and Release Hygiene
Removed sections:
- None
Templates requiring updates:
- ✅ .specify/templates/plan-template.md
- ✅ .specify/templates/spec-template.md
- ✅ .specify/templates/tasks-template.md
- ✅ README.md
Deferred items:
- None
-->

# STEM Constitution

## Core Principles

### I. Library-First Module Boundaries
Every feature and change MUST start in the lowest stable module that can own it.
- `stem-common` MUST contain shared contracts, DTOs, enums, and utilities that do not
require Spring runtime behavior. 
- `stem-core` MUST contain domain behavior,
persistence-facing logic, and internal services without HTTP or starter concerns.
- `stem-api` MUST contain API adapters only when an externally exposed transport layer is
required. 
- `stem-spring-boot-starter` MUST only provide Spring Boot integration and MUST
NOT become the primary home of business logic. Module dependencies MUST remain acyclic,
public contracts MUST stay minimal, and new abstractions MUST be justified by reuse or
stability needs rather than organization alone.

Rationale: a starter library survives through stable contracts and replaceable adapters,
not by coupling business logic to Boot wiring or web delivery details.

### II. Backwards Compatibility Is the Default
Published Maven coordinates, public Java types, configuration properties, bean contracts,
and documented runtime behavior MUST be treated as stable compatibility surfaces.
Additive changes are preferred. Breaking changes MUST ship only in a major version,
MUST include a migration guide, and MUST be preceded by at least one minor release of
documented deprecation unless a security or legal issue requires faster removal.
Behavioral changes to defaults MUST be called out explicitly in release notes and tests.

Rationale: consumers adopt starter libraries for predictable integration; accidental API
or behavior drift is a product failure even when the code still compiles.

### III. Auto-Configuration Must Be Explicit and Override-Friendly
Auto-configuration MUST register beans explicitly and conditionally. Starter code MUST use
`@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty`, and
dedicated `@ConfigurationProperties` where applicable so consumers can opt in, override,
or disable behavior safely. Broad component scanning, hidden side effects, eager external
connections, and automatic web endpoint exposure are prohibited unless they are the core
documented purpose of the library and guarded by explicit configuration. Default startup
behavior MUST be cheap, safe, and observable.

Rationale: a Spring Boot starter succeeds when it is unsurprising, narrowly scoped, and
easy for consuming applications to reason about.

### IV. Testing and Verification Are Non-Negotiable
Every change MUST include tests in the owning module, and bug fixes MUST begin with a
failing reproducer. Unit tests are mandatory for local behavior. Contract or compatibility
tests are mandatory when public APIs, serialization, configuration properties, or starter
behavior change. Auto-configuration changes MUST be verified with focused application
context tests such as `ApplicationContextRunner`-style coverage. Cross-module flows MUST
have integration coverage when wiring, persistence, or transport boundaries are affected.
No change may merge without deterministic test evidence from the Maven reactor.

Rationale: library defects are amplified downstream; disciplined verification is cheaper
than discovering regressions in consumer applications.

### V. Dependency, Observability, Security, and Documentation Hygiene
Dependencies MUST be minimal, centrally managed in the parent POM, and scoped as tightly
as possible. New transitive dependencies, version overrides, and new starters require a
written justification in the plan or pull request. Runtime behavior MUST emit actionable
signals through documented logs, metrics, or traces proportionate to the feature’s impact
without leaking secrets or generating noisy logs by default. Secure defaults, input
validation, least-privilege assumptions, and dependency vulnerability review are required.
Every externally visible change MUST update developer-facing documentation, including
module READMEs, configuration reference material, and release notes as applicable.

Rationale: starter libraries are shared infrastructure; dependency sprawl, silent runtime
behavior, poor docs, and insecure defaults all create downstream operational cost.

## Engineering Standards

- Java 17 and the Spring Boot 3.x baseline defined in the parent Maven build are the
  supported implementation targets unless the constitution is amended.
- All dependency versions MUST be controlled from the root `pom.xml` through properties or
  dependency management. Child modules MUST NOT pin duplicate versions without an explicit,
  documented exception.
- Public configuration keys MUST be namespaced under `stem.*`, documented with defaults,
  and generated as configuration metadata when surfaced by the starter.
- Lombok, MapStruct, JPA, and web dependencies MUST stay confined to the modules that need
  them; `stem-common` MUST remain free of avoidable Spring or persistence coupling.
- Logging MUST use library-safe practices: no secret material, no PII by default, and no
  INFO-level startup noise that a consuming application cannot suppress.
- Security-sensitive features MUST fail closed, validate untrusted input, and document any
  assumptions about authentication, authorization, tenancy, or data handling.
- Documentation for each module MUST explain its role, public entry points, configuration
  surface, and extension points. Examples MUST reflect supported usage rather than internal
  shortcuts.

## Delivery Workflow and Release Hygiene

- Feature specs and implementation plans MUST identify the impacted modules, the lowest
  viable owning module, compatibility implications, auto-configuration impact, dependency
  changes, observability expectations, and security considerations before implementation.
- The Constitution Check in every plan MUST fail the work item until module placement,
  compatibility, testing, starter conditions, documentation, and release impact are
  explicitly addressed.
- Pull requests MUST include: test evidence, documentation updates, compatibility notes for
  public surface changes, and release note text for any user-visible behavior change.
- Releases MUST be validated from the repository root with the full Maven reactor,
  including unit and integration suites required by the affected modules.
- Source artifacts, changelog or release notes, deprecations, and version alignment across
  all modules MUST be reviewed before publication.
- If a starter change alters bean registration, conditional logic, or configuration
  properties, release validation MUST include a consumer-style application context check.

## Governance

This constitution supersedes conflicting local practices for the STEM repository. Every
plan, pull request, code review, and release review MUST verify compliance with these
principles.

Amendments MUST be made in pull requests that include: the proposed constitutional text,
the rationale for the change, any required template or documentation updates, and a
migration plan when existing code or process must change to comply.

Versioning policy for this constitution follows semantic versioning:

- MAJOR: removal or redefinition of a principle or governance rule in a way that changes
  required behavior or invalidates existing process assumptions.
- MINOR: addition of a new principle, new mandatory section, or materially expanded
  guidance that introduces new compliance expectations.
- PATCH: clarifications, wording improvements, examples, or non-semantic refinements.

Compliance review expectations:

- Plans MUST record constitution checks before research completion and after design.
- Reviewers MUST block merges that violate module boundaries, compatibility guarantees,
  auto-configuration discipline, testing requirements, or release hygiene.
- Release managers MUST confirm documentation, deprecation notices, and dependency review
  are complete before publishing artifacts.

Operational guidance for day-to-day repository work lives in `README.md` and the current
`.specify/templates/*.md` workflow templates, which MUST remain aligned with this
constitution.

**Version**: 1.0.0 | **Ratified**: 2026-04-17 | **Last Amended**: 2026-04-17
