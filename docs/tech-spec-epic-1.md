# Epic Technical Specification: Foundation & Infrastructure Enablement

Date: 2025-11-10
Author: Nag
Epic ID: 1
Status: Draft

> **ВАЖНО**: Актуальные версии зависимостей см. в `docs/spring-modulith-2.0-setup-guide.md`

---

## Overview

Epic 1 устанавливает фундаментальную инфраструктуру для проекта Tech-Support: модульный монолит на базе Spring Boot 4.0.0-RC1 и Spring Modulith 2.0.0-RC1, CI/CD конвейер на GitHub Actions, контейнеризацию через Docker/Docker Compose и базовые механизмы наблюдаемости и безопасности. Эпик обеспечивает единую техническую платформу для последующих эпиков (2-8), закрепляя архитектурные принципы, стандарты разработки и операционные процессы, описанные в PRD и Architecture документах.

## Objectives and Scope

**В области охвата:**
- Инициализация Git-репозитория с модульной структурой Spring Modulith (модули: usermanagement, ticketing, audit, analytics, notification, knowledgebase)
- CI/CD pipeline на GitHub Actions с автоматизированной сборкой, тестами, quality gate (Sonar/аналог) и публикацией артефактов
- Docker Compose для локальной разработки (приложение + PostgreSQL 17 + Redis + pgAdmin)
- Multi-stage Dockerfile для сборки и развертывания приложения
- Observability stack: Prometheus metrics (/actuator/prometheus), структурированное JSON логирование с correlation ID
- Security baseline: TLS 1.3 конфигурация, управление секретами, session hardening

**Вне области охвата:**
- Бизнес-логика модулей (реализуется в Epic 2-8)
- Production deployment в ЦРБ инфраструктуру (Epic 1 готовит staging и локальные среды)
- Полная observability pipeline с Grafana/Loki (Epic 1 создаёт scaffold, интеграция — в рамках операционной фазы)
- API Gateway или Service Mesh (не требуются для модульного монолита)

## System Architecture Alignment

Эпик 1 напрямую реализует архитектурные решения ADR-001 (Modular Monolith), ADR-002 (Event-Driven Architecture), ADR-006 (Testing Strategy). Структура репозитория следует Spring Modulith conventions с четкими module boundaries и API/impl separation. CI/CD конвейер интегрирует Testcontainers для integration testing, JaCoCo для code coverage (80%+ target), Spotless для code formatting. Docker Compose обеспечивает dev/staging parity, соответствуя 12-factor app принципам. Security baseline включает конфигурации из Architecture doc (JWT placeholders, RBAC preparation, audit logging infrastructure).


## Detailed Design

### Services and Modules

| Module | Responsibility | Inputs/Outputs | Owner |
|--------|---------------|----------------|-------|
| **usermanagement** | Управление пользователями, ролями, аутентификация/авторизация | Input: регистрационные данные, credentials<br>Output: JWT tokens, user profiles, role assignments | Backend Team |
| **ticketing** | Управление жизненным циклом тикетов, статусами, комментариями | Input: ticket creation/update requests<br>Output: ticket entities, history events | Backend Team |
| **audit** | Неизменяемый журнал событий (WORM), compliance tracking | Input: domain events от всех модулей<br>Output: audit logs, integrity reports | Security/Compliance Team |
| **analytics** | Аналитическое хранилище, агрегация метрик, отчёты | Input: ticket data, user activity<br>Output: dashboards, KPI reports | Data Team |
| **notification** | Мульти-канальные уведомления (Telegram, Email), delivery tracking | Input: notification events<br>Output: delivery status, retry queue | Backend Team |
| **knowledgebase** | База знаний, статьи, поиск по контенту (placeholder для Epic 8) | Input: articles, search queries<br>Output: search results, article metadata | Content Team |

**Inter-Module Communication:**
- Все модули взаимодействуют через Spring Modulith Events (async, transactional)
- Event Publication Registry обеспечивает guaranteed delivery
- API boundaries проверяются через Modulith Boundary Tests

### Data Models and Contracts

**Database Schema Versioning:**
- Flyway migrations в src/main/resources/db/migration/
- Naming: V1__init_schema.sql, V2__add_audit_table.sql
- Каждый модуль имеет префикс таблиц: um_users, tk_tickets, au_audit_log

### APIs and Interfaces

**Epic 1 создаёт базовую API структуру (без реализации бизнес-логики):**

Actuator endpoints (configured in application.yml):
- management.endpoints.web.exposure.include=health,info,prometheus,metrics
- management.endpoint.health.show-details=when-authorized

### Workflows and Sequencing

**Story 1.1: Repository Initialization Flow**
1. Create Git repo with .gitignore (Gradle, IDE, OS-specific)
2. Initialize Gradle multi-module project structure
3. Configure Spring Boot Gradle plugin with dependency management
4. Create module directories: usermanagement/, ticketing/, audit/, etc.
5. Add @ApplicationModule annotations to package-info.java
6. Create Modulith Boundary Tests in src/test/java/
7. Run gradle build to validate structure

**Story 1.2: CI/CD Pipeline Flow**
1. Push to feature branch → GitHub Actions trigger
2. Cache Gradle dependencies (~/.gradle/caches, ~/.gradle/wrapper)
3. Run gradle clean build (compile + test + integration tests)
4. Run gradle spotlessCheck (code formatting)
5. Run JaCoCo coverage report
6. Upload coverage to Sonar (quality gate check)
7. If PR to main → require passing checks + approvals
8. Merge → build Docker image → push to registry (staging)

**Story 1.3: Local Development Flow**
1. Developer runs docker-compose up -d
2. PostgreSQL + Redis containers start
3. Flyway migrations auto-run on app startup
4. Spring Boot app listens on :8080
5. Developer makes code changes
6. Hot reload via Spring DevTools
7. Run docker-compose down to cleanup

**Story 1.4: Observability Flow**
1. Request arrives → MDC adds correlation_id
2. Logback writes structured JSON logs
3. Micrometer records metrics (HTTP latency, JVM memory)
4. Prometheus scrapes /actuator/prometheus every 15s
5. Grafana queries Prometheus for dashboard visualization
6. Alerts trigger on anomalies (e.g., p95 latency > 500ms)


## Non-Functional Requirements

### Performance

**Build Performance:**
- Gradle clean build completes in ≤3 минут (without cache), ≤1 минута (with build cache)
- Docker image build time: ≤10 минут при наличии layer cache
- CI/CD pipeline полный цикл: ≤15 минут от push до deployment

**Runtime Performance (baseline для Epic 1):**
- Application startup time: ≤30 секунд
- Health check response: ≤100 мс
- Prometheus metrics scrape: ≤200 мс

**Scalability Targets:**
- Поддержка до 50 concurrent developers в локальных средах
- CI/CD pipeline выдерживает ≥10 simultaneous builds

### Security

**Development Security:**
- Все secrets управляются через GitHub Secrets (CI/CD) и .env файлы (local, git-ignored)
- TLS 1.3 для всех HTTPS endpoints (Nginx reverse proxy configuration)
- Security headers: HSTS, X-Content-Type-Options, X-Frame-Options, CSP
- Session configuration: HttpOnly cookies, SameSite=Strict, 30-минутный timeout

**Code Security:**
- Dependency scanning через Dependabot (GitHub)
- SAST через Sonar Security Hotspots
- No hardcoded credentials (проверяется pre-commit hooks)

**Infrastructure Security:**
- Docker base images: официальные Eclipse Temurin для Java
- PostgreSQL authentication через password (dev) или certificates (prod preparation)
- Secrets rotation procedure документирована в SECURITY.md

### Reliability/Availability

**Local Development:**
- Docker Compose должен восстанавливаться после перезагрузки хоста
- Health checks для всех контейнеров (PostgreSQL, Redis, app)
- Automatic restart policies в docker-compose.yml

**CI/CD Reliability:**
- Retry logic для flaky tests (максимум 2 retries)
- Fail-fast на критических ошибках (compile, security vulnerabilities)
- Branch protection: require passing checks + 1 approval для main

**Data Durability:**
- PostgreSQL data volumes persist между docker-compose restarts
- Flyway migrations идемпотентны и версионированы
- Backup strategy документирована (для production, Epic 1 создаёт scaffold)

### Observability

**Logging:**
- Structured JSON logs с полями: timestamp, level, logger, message, correlation_id, user_id (masked)
- Log levels: ERROR (production), DEBUG (local development)
- Rotation: 10MB per file, 7 days retention (local), centralized logging (staging/prod)

**Metrics:**
- JVM metrics: heap/non-heap memory, GC pauses, thread count
- HTTP metrics: request count, latency (p50, p95, p99), error rate
- Custom business metrics: module event counts, database connection pool

**Tracing:**
- Correlation ID propagation через MDC (Mapped Diagnostic Context)
- Request/response logging в DEBUG mode
- Distributed tracing scaffold (OpenTelemetry preparation для Epic 2+)

**Monitoring:**
- Prometheus scrape interval: 15 секунд
- Grafana dashboards: JVM overview, HTTP traffic, database connections
- Alert thresholds: CPU >80%, memory >85%, p95 latency >500ms


## Dependencies and Integrations

> **ВАЖНО**: См. актуальную конфигурацию в `docs/spring-modulith-2.0-setup-guide.md`

### Core Dependencies (gradle/libs.versions.toml)

**Spring Boot Ecosystem:**
- org.springframework.boot:spring-boot-starter-parent:4.0.0-RC1 (BOM)
- org.springframework.boot:spring-boot-starter-web:4.0.0-RC1
- org.springframework.boot:spring-boot-starter-data-jpa:4.0.0-RC1
- org.springframework.boot:spring-boot-starter-security:4.0.0-RC1
- org.springframework.boot:spring-boot-starter-validation:4.0.0-RC1
- org.springframework.boot:spring-boot-starter-actuator:4.0.0-RC1

**Spring Modulith:**
- org.springframework.modulith:spring-modulith-bom:2.0.0-RC1 (управление версиями)
- org.springframework.modulith:spring-modulith-starter-core:2.0.0-RC1
- org.springframework.modulith:spring-modulith-starter-jpa:2.0.0-RC1
- org.springframework.modulith:spring-modulith-events-jdbc:2.0.0-RC1
- org.springframework.modulith:spring-modulith-observability:2.0.0-RC1 (runtime)
- org.springframework.modulith:spring-modulith-actuator:2.0.0-RC1 (runtime)

**Database & Persistence:**
- org.postgresql:postgresql:42.7.4 (runtime)
- org.flywaydb:flyway-core:11.1.0
- org.flywaydb:flyway-database-postgresql:11.1.0

**Testing:**
- org.springframework.boot:spring-boot-starter-test:4.0.0-RC1 (test scope)
- org.springframework.modulith:spring-modulith-starter-test:2.0.0-RC1 (test scope)
- org.testcontainers:testcontainers-bom:1.20.4 (import, test scope)
- org.testcontainers:postgresql:1.20.4 (test scope)
- org.testcontainers:junit-jupiter:1.20.4 (test scope)

**Observability:**
- io.micrometer:micrometer-registry-prometheus:1.14.2 (runtime)
- ch.qos.logback:logback-classic:1.5.12 (provided by Spring Boot)
- net.logstash.logback:logstash-logback-encoder:8.0 (для JSON logs)

**Code Quality:**
- com.diffplug.spotless:spotless-gradle-plugin:6.25.0 (build plugin)
- org.jacoco:jacoco-maven-plugin:0.8.12 (build plugin)
- com.github.spotbugs:spotbugs-maven-plugin:4.8.6.4 (optional)

### External Integrations

**GitHub Actions:**
- actions/checkout@v4 - клонирование репозитория
- actions/setup-java@v4 - установка Java 21
- actions/cache@v4 - кэширование Maven dependencies
- docker/build-push-action@v6 - сборка и публикация Docker images

**Docker Registry:**
- GitHub Container Registry (ghcr.io) для хранения images
- Автоматическая публикация при merge в main
- Tagging strategy: latest, git-sha, semantic version

**SonarCloud/SonarQube:**
- Интеграция через Gradle plugin: org.sonarqube:sonarqube-gradle-plugin:6.0.1.5171
- Quality Gate parameters: coverage ≥80%, no blockers, no critical issues
- Token authentication через GitHub Secrets

**Dependabot:**
- Автоматические PR для обновления зависимостей
- Настроен для Gradle ecosystem
- Weekly check schedule

### Infrastructure Dependencies

**Local Development:**
- Docker Engine 27.x+
- Docker Compose 2.30.x+
- Gradle 8.11.1
- Java 21 LTS (Eclipse Temurin recommended)

**CI/CD:**
- GitHub Actions runners (ubuntu-latest)
- PostgreSQL service container для integration tests
- Redis service container (optional, для будущих эпиков)

**Monitoring Stack (scaffold):**
- Prometheus 2.55.x (future deployment)
- Grafana 11.4.x (future deployment)
- Node Exporter для host metrics (production)


## Acceptance Criteria (Authoritative)

### Story 1.1: Инициализировать модульный монолит и структуру репозитория

**AC-1.1.1:** В репозитории создан монолит на Spring Boot 4.0.0-RC1 + Spring Modulith 2.0.0-RC1 с модулями usermanagement, ticketing, audit, analytics, notification, knowledgebase и разделением API/impl.

**AC-1.1.2:** Настроены Gradle-зависимости (Gradle 8.11.1), Spring Modulith BOM, репозиторий Spring Milestone, профили local, staging, prod, плагины Flyway, Testcontainers, Spotless и JaCoCo.

**AC-1.1.3:** Включены базовые тесты Modulith Boundary Test и шаблоны README.md и CONTRIBUTING.md.

**AC-1.1.4:** Результаты сборки успешно проходят локальный gradle build.

### Story 1.2: Настроить GitHub Actions CI с качественным шлюзом

**AC-1.2.1:** Workflow GitHub Actions запускает сборку на Java 21 при создании PR и push в main.

**AC-1.2.2:** Выполняются цели gradle build, spotlessCheck, публикуется отчёт JaCoCo как артефакт.

**AC-1.2.3:** Интегрирован Sonar (или аналогичный quality gate) через secrets, статус блокирует merge при сбоях.

**AC-1.2.4:** Кэшируются зависимости Gradle (~/.gradle/caches) и Docker-слои, оптимизирован build cache для ускорения сборки.

### Story 1.3: Подготовить Docker Compose и локальные среды

**AC-1.3.1:** В корне репозитория создан docker-compose.yml, поднимающий приложение, PostgreSQL 17, Redis и опционально pgAdmin.

**AC-1.3.2:** Multi-stage Dockerfile собирает приложение, профиль Spring local активируется через переменные окружения.

**AC-1.3.3:** README содержит инструкции по переменным, запуску миграций Flyway и healthcheck.

**AC-1.3.4:** Предоставлен PowerShell/Make-скрипт для docker compose up/down и очистки.

### Story 1.4: Включить наблюдаемость и структурированное логирование

**AC-1.4.1:** Эндпоинт /actuator/prometheus возвращает метрики Micrometer (JVM, HTTP, бизнес-события).

**AC-1.4.2:** Логи пишутся в JSON с полями timestamp, level, user_id, correlation_id, маскируются PII.

**AC-1.4.3:** Документация описывает настройки ротации, интеграцию с лог-агентом и Grafana dashboard scaffold.

**AC-1.4.4:** Набор дашбордов для локальной отладки и preview окружения хранится в docs/observability.

### Story 1.5: Настроить базовую безопасность и управление секретами

**AC-1.5.1:** Подготовлен шаблон application-security.yaml с настройками security headers, CSRF и session-hardening.

**AC-1.5.2:** Документация описывает загрузку секретов из защищённых переменных/хранилищ и регулярную ротацию.

**AC-1.5.3:** Прокси (Nginx/Ingress) настроен на TLS 1.3, валидацию сертификатов и HSTS.

**AC-1.5.4:** Созданы Ansible/PowerShell плейбуки для начального развёртывания и ротации секретов.

## Traceability Mapping

| AC ID | Spec Section | Component/API | Test Idea |
|-------|-------------|---------------|-----------|
| AC-1.1.1 | Detailed Design > Services and Modules | Gradle modules, @ApplicationModule | ModulithBoundaryTest verifies module isolation |
| AC-1.1.2 | Dependencies > Core Dependencies | build.gradle | Gradle dependencies task, profiles activation test |
| AC-1.1.3 | Detailed Design > Workflows | README.md, CONTRIBUTING.md | Template presence check, markdown lint |
| AC-1.1.4 | NFR > Performance | gradle build | CI/CD pipeline execution, build time measurement |
| AC-1.2.1 | Dependencies > GitHub Actions | .github/workflows/ci.yml | Workflow syntax validation, trigger simulation |
| AC-1.2.2 | Dependencies > Code Quality | Gradle plugins | Artifact upload verification, coverage report parsing |
| AC-1.2.3 | Dependencies > SonarCloud | sonar-project.properties | Quality gate API mock, threshold validation |
| AC-1.2.4 | NFR > Performance | actions/cache@v4 | Cache hit rate measurement, build time comparison |
| AC-1.3.1 | Detailed Design > Workflows | docker-compose.yml | docker-compose config validation, service health checks |
| AC-1.3.2 | Dependencies > Infrastructure | Dockerfile | Multi-stage build test, image size verification |
| AC-1.3.3 | Detailed Design > Workflows | README.md | Documentation completeness check, command validation |
| AC-1.3.4 | Detailed Design > Workflows | scripts/ | Script execution tests, error handling verification |
| AC-1.4.1 | APIs and Interfaces | /actuator/prometheus | HTTP GET test, metrics format validation (OpenMetrics) |
| AC-1.4.2 | NFR > Observability | Logback configuration | JSON schema validation, PII masking test |
| AC-1.4.3 | NFR > Observability | docs/observability/ | Documentation presence, Grafana JSON validity |
| AC-1.4.4 | NFR > Observability | Dashboard templates | Grafana API import test, query syntax validation |
| AC-1.5.1 | NFR > Security | application-security.yaml | Security headers response test, CSRF token validation |
| AC-1.5.2 | NFR > Security | SECURITY.md | Documentation completeness, secrets management workflow |
| AC-1.5.3 | NFR > Security | nginx.conf | TLS configuration test, HSTS header verification |
| AC-1.5.4 | NFR > Security | ansible/ | Playbook syntax check, idempotency test |


## Risks, Assumptions, Open Questions

### Risks

**RISK-1:** Spring Modulith 2.0 RC1 может иметь breaking changes до GA (2025-11-21).
- *Mitigation:* Мониторинг release notes, готовность к быстрому обновлению зависимостей. RC1 считается production-ready согласно Spring team.
- *Impact:* Средний (требует переработки конфигураций)
- *Probability:* Низкая (RC обычно стабильны)

**RISK-2:** GitHub Actions runners могут испытывать перебои, задерживая CI/CD.
- *Mitigation:* Настроить self-hosted runners как fallback опцию. Использовать retry logic для flaky steps.
- *Impact:* Низкий (временная задержка, не блокирует разработку)
- *Probability:* Средняя (случайные инциденты GitHub)

**RISK-3:** Docker Desktop licensing ограничения для команды >250 человек.
- *Mitigation:* Проект использует Podman или Rancher Desktop как альтернативу. Документировать оба варианта.
- *Impact:* Низкий (альтернативы доступны)
- *Probability:* Низкая (команда <10 человек)

**RISK-4:** Недостаточное покрытие тестами на ранней стадии замедляет обнаружение багов.
- *Mitigation:* Установить quality gate coverage ≥80% с первого эпика. Обязательные code reviews.
- *Impact:* Высокий (технический долг, регрессии)
- *Probability:* Средняя (давление на сроки)

### Assumptions

**ASSUMPTION-1:** Команда имеет доступ к GitHub organization с Actions enabled.
- *Validation:* Проверить permissions до начала Story 1.2.

**ASSUMPTION-2:** ЦРБ инфраструктура поддерживает Docker и имеет Linux servers с Docker Engine.
- *Validation:* Согласовать с IT отделом до Story 1.3.

**ASSUMPTION-3:** Разработчики имеют локальные машины с ≥16GB RAM для Docker Compose.
- *Validation:* Провести environment survey перед началом Epic 1.

**ASSUMPTION-4:** SonarCloud/SonarQube лицензия доступна для проекта.
- *Validation:* Альтернатива: использовать только JaCoCo + Spotless без Sonar.

### Open Questions

**QUESTION-1:** Использовать Maven или Gradle?
- *Resolution:* Gradle 8.11.1 (указано в Architecture doc). Обеспечивает лучший build cache и производительность.

**QUESTION-2:** Нужен ли отдельный staging environment или достаточно локального Docker Compose?
- *Next Step:* Согласовать с PM и DevOps в начале Story 1.3.

**QUESTION-3:** Какой Sonar instance использовать: SonarCloud (SaaS) или self-hosted SonarQube?
- *Next Step:* Согласовать с Security team в Story 1.2.

## Test Strategy Summary

### Test Pyramid

**Unit Tests (70% coverage target):**
- JUnit 5 для всех service/utility классов
- Mockito для mocking dependencies
- AssertJ для fluent assertions
- Execution time: <5 секунд для полного suite

**Integration Tests (25% coverage target):**
- Spring Boot Test для API endpoints
- Testcontainers PostgreSQL для database tests
- Spring Modulith Test для module boundary verification
- Execution time: <2 минут для полного suite

**E2E Tests (5% coverage target, Epic 1 scaffold только):**
- Placeholder для будущих Selenium/Playwright tests
- Smoke test: docker-compose up → health check → docker-compose down
- Execution time: <5 минут

### Test Automation

**CI/CD Integration:**
- Все тесты запускаются на каждый push/PR
- Parallel execution: unit tests → integration tests (with DB container)
- Fail-fast: блокировать merge при failed tests
- Coverage report публикуется как artifact

**Local Development:**
- mvn test для быстрых unit tests
- mvn verify для full test suite (включая integration)
- IDE integration: IntelliJ/VSCode test runners

### Test Data Management

**Unit Tests:**
- In-memory data structures
- Builder patterns для test fixtures
- Faker library для realistic data generation (optional)

**Integration Tests:**
- Flyway migrations создают schema
- SQL scripts в src/test/resources/data/ для seed data
- @Sql annotations для test-specific data setup
- Testcontainers обеспечивает clean state для каждого теста

### Quality Gates

**Pre-Merge Requirements:**
- ✅ All tests pass
- ✅ Code coverage ≥80% (lines)
- ✅ No Sonar blockers/critical issues
- ✅ Spotless formatting applied
- ✅ 1+ code review approval

**Post-Merge Monitoring:**
- Build time trending (не должно расти >10% за sprint)
- Flaky test detection и mitigation
- Test coverage trends (dashboard в README)

### Testing Tools

- **Frameworks:** JUnit 5, Spring Boot Test, Testcontainers
- **Mocking:** Mockito, WireMock (для future HTTP mocks)
- **Assertions:** AssertJ, Hamcrest
- **Coverage:** JaCoCo, Sonar
- **Performance:** JMH для microbenchmarks (optional)

---

**Document Status:** Ready for Review
**Next Steps:** Validate against checklist, begin Story 1.1 implementation

