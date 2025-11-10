# Tech Spec: Story 1.1 - Инициализировать модульный монолит и структуру репозитория

> **ВАЖНО**: Актуальная информация по настройке зависимостей в `docs/spring-modulith-2.0-setup-guide.md`

## Context

Primary Context: This story is implemented against the following primary documents:

- `docs/tech-spec-epic-1.md` (Epic-level technical specification)
- `docs/architecture-2025-11-06.md` (ADR-001 Modular Monolith)
- `docs/epics.md` (Story 1.1 definition and acceptance criteria)
- `docs/spring-modulith-2.0-setup-guide.md` (Setup guide for Spring Modulith 2.0 RC1)

Project Type: Greenfield - no existing codebase. All files are created from scratch.

Target Stack (summary):

- Java 21 (toolchain)
- Gradle 8.11.1 with version catalog (`gradle/libs.versions.toml`)
- Spring Boot 4.0.0-RC1
- Spring Modulith 2.0.0-RC1
- PostgreSQL 17, JDBC driver 42.7.4
- Flyway 11.1.0
- Testcontainers BOM 1.20.4

## Development Context

This tech-spec contains explicit CREATE/UPDATE actions required by the validation checklist so a developer can start immediately.

### Files to CREATE (explicit actions)

- CREATE `build.gradle` - Project build configuration, plugin versions, dependency management
- CREATE `settings.gradle` - rootProject.name = 'tech-support'
- CREATE `gradle/libs.versions.toml` - Version catalog
- CREATE `gradle.properties` - profiles and build properties
- CREATE `.gitignore` - Gradle, IDE, OS ignores
- CREATE `README.md`, `CONTRIBUTING.md`
- CREATE `src/main/java/com/techsupport/TechSupportApplication.java`
- CREATE `src/main/java/com/techsupport/{module}/package-info.java` for modules: usermanagement, ticketing, audit, analytics, notification, knowledgebase
- CREATE `src/main/java/com/techsupport/{module}/api/.gitkeep` and `.../impl/.gitkeep` for each module
- CREATE `src/main/resources/application*.yml` (application.yml, application-local.yml, application-staging.yml, application-prod.yml)
- CREATE `src/main/resources/db/migration/.gitkeep`
- CREATE `src/test/java/com/techsupport/ModulithBoundaryTest.java` and `src/test/resources/application-test.yml`

### Exact CREATE examples (file-by-file)

- CREATE `src/main/java/com/techsupport/TechSupportApplication.java` - main class with `@SpringBootApplication`
- CREATE `src/main/java/com/techsupport/usermanagement/package-info.java` with `@org.springframework.modulith.ApplicationModule(displayName = "User Management")`
- CREATE `src/main/java/com/techsupport/usermanagement/api/.gitkeep`
- CREATE `src/main/java/com/techsupport/usermanagement/impl/.gitkeep`

Repeat equivalent CREATE actions for: ticketing, audit, analytics, notification, knowledgebase.

## Developer Resources

### Code Examples

package-info.java template:

```java
@org.springframework.modulith.ApplicationModule(
    displayName = "User Management"
)
package com.techsupport.usermanagement;
```

TechSupportApplication.java template and build.gradle starter are available in `docs/stories/1-1-initializirovat-modulnyj-monolit-i-strukturu-repozitoriya.md` Dev Notes.

## Acceptance Criteria Mapping

- AC-1.1.1: fulfilled by CREATE actions for modules and TechSupportApplication.java
- AC-1.1.2: fulfilled by CREATE actions for build.gradle, libs.versions.toml, gradle.properties, application*.yml
- AC-1.1.3: fulfilled by CREATE actions for ModulithBoundaryTest and README/CONTRIBUTING templates
- AC-1.1.4: verified by running `./gradlew clean build` (local validation step)

## Notes

This file is a story-level tech-spec that complements the Epic-level tech-spec. It contains the explicit file actions and code examples required by the validation checklist.
