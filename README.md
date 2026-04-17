# stem

STEM is a multi-module Maven project for a Spring Boot-friendly task engine library.

## Module layout

- `stem-common`: shared contracts, DTOs, enums, and utility types
- `stem-core`: domain logic, lifecycle handling, and persistence-facing behavior
- `stem-api`: API adapters for externally exposed transport concerns
- `stem-spring-boot-starter`: opt-in Spring Boot auto-configuration for consuming apps

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
