# Technical Research Report: Tech-Support System

**Project:** Tech-Support Helpdesk for ЦРБ Марьина Горка
**Date:** 2025-11-05
**Analyst:** Business Analyst Mary
**Stakeholder:** Nag

---

## Executive Summary

This technical research report validates the technology stack for a modular monolith helpdesk system serving approximately 400 users at a medical facility. The research focused on selecting production-ready, AI-assisted development-friendly technologies that balance cutting-edge features with long-term stability.

**Key Decisions:**
- **Java 21 LTS** - Long-term support until 2029, production-safe foundation
- **Spring Modulith 2.0.0-RC1** - Pre-release with GA planned November 21, 2025
- **Spring Boot 4.0.0-RC1** - Latest major version aligned with Modulith 2.0
- **PostgreSQL 17.6** - Conservative choice with proven ACID guarantees
- **Spring Security 6.x** - OAuth2/OIDC ready for future Keycloak migration

**Risk Assessment:** Medium - Early adoption of Spring Modulith 2.0 RC1 mitigated by near-term GA release (2 weeks) and upgrade path.

**Recommendation:** Proceed with selected stack. Begin development with RC1, upgrade to GA on November 21, 2025, deploy to production Q1 2026.

---

## 1. Requirements Summary

### 1.1 Functional Requirements

**Core Ticket Management:**
1. Ticket Creation - Employees submit issues with description, category, priority
2. Ticket Assignment - Support staff claim/assigned tickets by supervisors
3. Ticket Resolution - Close tickets with resolution notes
4. Ticket Search & Filtering - By status, category, assignee, date range
5. Ticket History - Full audit trail of all changes

**User Management:**
6. User Registration - Self-registration with approval workflow
7. Role Management - Three roles: Employee, Support, Admin
8. User Profile - Contact info, department, location

**Audit & Analytics:**
9. Audit Logging - Immutable log of all system actions
10. Basic Analytics - Ticket volume, resolution time, category distribution

**Notifications:**
11. Real-time Notifications - Telegram/Viber integration for ticket updates

**Knowledge Base (Future):**
12. FAQ Module - Self-service knowledge base (stub for future implementation)

### 1.2 Non-Functional Requirements

**Performance:**
- Response Time: <300ms for 95% of requests under normal load
- Throughput: 50 concurrent users, 500 daily active users peak
- Database: 10,000 tickets/year, 5-year retention = 50K tickets total
- Scalability: Vertical scaling sufficient (400 users), horizontal ready if needed

**Reliability:**
- Uptime: 99.5% (medical facility, non-critical system)
- Recovery Time Objective (RTO): <4 hours
- Recovery Point Objective (RPO): <1 hour (daily backups + WAL archiving)
- Failover: PostgreSQL replication for high availability (optional Phase 2)

**Security:**
- Authentication: Username/password (BCrypt), session-based
- Authorization: RBAC (3 roles) + ownership-based ABAC
- Data Protection: TLS 1.3 in transit, AES-256 at rest (PostgreSQL encryption)
- Audit: GDPR-compliant immutable audit logs (employee data, not patient data)
- Compliance: Personal data of employees only - simplified vs healthcare patient data

**Maintainability:**
- Code Coverage: 80%+ for service layer (TDD approach)
- Documentation: API docs (Swagger/SpringDoc), architecture diagrams, README
- Modularity: Spring Modulith logical modules with clear bounded contexts
- Testing: Unit tests, integration tests (Testcontainers), E2E tests (Playwright/Selenium)

**Usability:**
- Responsive Design: Desktop-first, mobile-friendly
- Accessibility: WCAG 2.1 AA compliance (public sector)
- Localization: Russian (primary), Belarusian (future)
- User Training: Admin documentation, user guides

**Operational:**
- Deployment: Docker containers, Docker Compose for local/staging
- Monitoring: Micrometer + Prometheus + Grafana stack
- Logging: Structured JSON logs (ELK stack optional)
- Backup: Automated daily PostgreSQL dumps + WAL archiving
- CI/CD: GitHub Actions for build, test, deploy pipeline

### 1.3 Technical Constraints

**Technology Stack:**
1. Backend: Java 21 LTS, Spring Boot 4.0
2. Architecture: Modular monolith (Spring Modulith 2.0)
3. Database: PostgreSQL 17.6
4. Testing: JUnit 5, Testcontainers, Mockito
5. Build: Maven 3.9+
6. Containerization: Docker, Docker Compose
7. Version Control: Git, GitHub

**Infrastructure:**
8. Deployment Target: On-premise server (ЦРБ infrastructure)
9. CI/CD: GitHub Actions (cloud-based builds)

**Team:**
10. Solo developer + AI-assisted development (GitHub Copilot, Claude)
11. Experience: Strong Java, Spring Boot, PostgreSQL
12. Learning Goals: DDD, Spring Modulith, TDD practices

**Timeline:**
13. Development: 3-4 months (Q4 2025 - Q1 2026)
14. Production Deployment: March 2026 target

### 1.4 Integration & UX Requirements

**External Integrations:**
1. Telegram Bot API - Real-time notifications for ticket updates
2. Viber Bot API - Alternative notification channel
3. OAuth2/OIDC Interface - Future Keycloak integration (not Phase 1)
4. SMTP/Email - Fallback notification channel

**User Experience:**
5. Single Page Application (SPA) - React or Vue.js frontend (TBD)
6. RESTful API - JSON-based, versioned (/api/v1/...)
7. Real-time Updates - Server-Sent Events (SSE) or WebSocket for live ticket updates
8. File Attachments - Screenshots/documents for tickets (MinIO or filesystem)

---

## 2. Technology Options Evaluated

### 2.1 Java Version

**Options Considered:**

| Version | Release Date | LTS | Support Until | Status |
|---------|--------------|-----|---------------|--------|
| Java 21 | Sept 2023 | Yes | Sept 2029 | **SELECTED** |
| Java 25 | Sept 2025 | No | March 2026 | Rejected |
| Java 24 | March 2025 | No | Sept 2025 | Rejected (EOL) |
| Java 17 | Sept 2021 | Yes | Sept 2027 | Considered |

**Decision Rationale:**
- ✅ Java 21 LTS provides 4 years of support (until 2029)
- ✅ Required by Spring Boot 4.0.0-RC1 (minimum Java 21)
- ✅ Production-safe for medical facility deployment
- ✅ Modern features: Virtual Threads (Project Loom), Pattern Matching, Records
- ❌ Java 24/25 non-LTS unsuitable for long-term production

### 2.2 Spring Boot Version

**Options Considered:**

| Version | Release Date | Spring Framework | Minimum Java | Status |
|---------|--------------|------------------|--------------|--------|
| 4.0.0-RC1 | Oct 2025 | 7.0.0-RC1 | Java 21 | **SELECTED** |
| 3.5.7 | Nov 2025 | 6.2.x | Java 17 | Stable Alternative |

**Decision Rationale:**
- ✅ Aligned with Spring Modulith 2.0.0-RC1 (requires Boot 4.0+)
- ✅ Latest ecosystem features (Framework 7.0, Jakarta EE 11)
- ✅ RC1 stability sufficient for Q1 2026 production (GA expected Nov 2025)
- ⚠️ Cutting-edge choice - mitigated by upgrade path to GA

### 2.3 Spring Modulith Version

**Options Considered:**

| Version | Release Date | Spring Boot | Key Features | Status |
|---------|--------------|-------------|--------------|--------|
| 2.0.0-RC1 | Oct 2025 | 4.0.0-RC1 | Flyway module migrations, runtime verification | **SELECTED** |
| 1.4.4 | Nov 2025 | 3.3.x | Stable, production-proven | Rejected |

**Key Features (2.0.0-RC1):**
- Flyway support for module-specific database migrations
- Runtime module structure verification
- Revamped event publication registry (JPA, JDBC, MongoDB, Neo4j)
- Jackson 3 support
- Spring Framework 7.0 / Boot 4.0 compatibility

**Decision Rationale:**
- ✅ GA release November 21, 2025 (2 weeks away)
- ✅ Module-specific Flyway migrations critical for modular schema evolution
- ✅ Runtime verification catches module boundary violations early
- ✅ Aligned with Spring Boot 4.0 ecosystem
- ⚠️ RC1 risk mitigated by near-term GA and upgrade strategy
- ✅ AI-assisted development benefits from latest features

**Upgrade Strategy:**
1. **November 5-20, 2025:** Develop with 2.0.0-RC1
2. **November 21, 2025:** Upgrade to 2.0 GA immediately upon release
3. **December 2025 - February 2026:** Stabilize on GA, full testing
4. **March 2026:** Production deployment

### 2.4 Database

**Options Considered:**

| Database | Version | Release Date | Key Features | Status |
|----------|---------|--------------|--------------|--------|
| PostgreSQL 17.6 | Nov 2025 | JSON improvements, parallel vacuuming | **SELECTED** |
| PostgreSQL 18.0 | Sept 2025 | 3× I/O performance, UUIDv7() | Considered |
| MySQL 8.x | - | InnoDB, JSON support | Rejected |
| MongoDB 7.x | - | Document flexibility | Rejected |

**Decision Rationale:**
- ✅ ACID transactions critical (ticket assignment + audit in single transaction)
- ✅ PostgreSQL 17.6 production-stable (conservative choice)
- ✅ Strong Spring Data JPA support
- ✅ JSON columns for flexible audit event storage
- ✅ Full-text search for ticket/knowledge base content
- ❌ PostgreSQL 18.0 rejected (too new, 3× I/O not critical for 400 users)
- ❌ MongoDB rejected (no ACID guarantees for transactional operations)

### 2.5 Authentication & Authorization

**Options Considered:**

| Solution | Complexity | Features | Status |
|----------|------------|----------|--------|
| Spring Security 6.x | Low | RBAC, OAuth2/OIDC ready | **SELECTED** |
| Keycloak | High | SSO, LDAP/AD, full IAM | Future Phase 2 |

**Decision Rationale:**
- ✅ Spring Security sufficient for 400 users, 3 roles
- ✅ OAuth2/OIDC interface allows future Keycloak migration
- ✅ Solo developer cannot maintain Keycloak infrastructure initially
- ✅ No SSO requirement in Phase 1
- 🔜 Keycloak migration when SSO or Active Directory integration needed

---

## 3. Technology Profiles

### 3.1 Java 21 LTS

**Overview:**
Java 21 is the latest Long-Term Support release, providing stability and modern language features for enterprise applications.

**Key Features:**
- **Virtual Threads (Project Loom):** Lightweight concurrency for high-throughput applications
- **Pattern Matching for switch:** Exhaustive handling of sealed types
- **Record Patterns:** Deconstruction of records in pattern matching
- **String Templates (Preview):** Safe string interpolation
- **Sequenced Collections:** Predictable iteration order APIs

**Suitability for Project:**
- ✅ LTS support until September 2029 (4 years)
- ✅ Required by Spring Boot 4.0
- ✅ Records perfect for immutable domain models (Ticket, User, AuditLog)
- ✅ Virtual Threads handle blocking I/O efficiently (database, external APIs)
- ✅ Pattern matching improves AI-generated code clarity

**Risks:**
- ⚠️ Newer than Java 17 LTS - less mature ecosystem (mitigated by Spring Boot 4.0 support)

**Verdict:** **Recommended** - Production-ready LTS with modern features ideal for AI-assisted development.

---

### 3.2 Spring Boot 4.0.0-RC1 + Spring Framework 7.0.0-RC1

**Overview:**
Spring Boot 4.0 is the next major version, bringing Jakarta EE 11, Spring Framework 7.0, and Java 21 baseline.

**Key Features:**
- **Jakarta EE 11:** Latest enterprise Java specifications
- **Spring Framework 7.0:** Core container and dependency injection improvements
- **Minimum Java 21:** Leverages modern JVM features
- **Enhanced Observability:** Micrometer 2.0, improved metrics and tracing
- **Virtual Thread Support:** Optimized for Project Loom

**Suitability for Project:**
- ✅ Aligned with Spring Modulith 2.0 (requires Boot 4.0)
- ✅ Latest ecosystem features for greenfield project
- ✅ RC1 stability sufficient for Q1 2026 deployment
- ✅ Strong community support and documentation

**Risks:**
- ⚠️ RC1 may have minor bugs - mitigated by GA release expected November 2025
- ⚠️ Less Stack Overflow content vs Boot 3.x - mitigated by AI assistance (Copilot trained on patterns)

**Verdict:** **Recommended** - Cutting-edge but low-risk given near-term GA and AI-assisted development.

---

### 3.3 Spring Modulith 2.0.0-RC1

**Overview:**
Spring Modulith enables logical modularization within a monolith, enforcing module boundaries through runtime verification and compile-time checks.

**Key Features (2.0.0-RC1):**
- **Flyway Module Migrations:** Each module manages its own database schema evolution
- **Runtime Verification:** Detects violations of module boundaries at startup
- **Event Publication Registry:** Supports JPA, JDBC, MongoDB, Neo4j event stores
- **Integration Testing:** Test individual modules in isolation
- **Architecture Documentation:** Auto-generates module dependency diagrams

**Module Design for Tech-Support:**

\\\
tech-support/
├── usermanagement/       # User, UserRole bounded context
├── ticketing/            # Ticket, TicketCategory, TicketStatus
├── audit/                # AuditLog - immutable event store
├── analytics/            # TicketMetrics - aggregated views
├── notification/         # TelegramService, ViberService
└── knowledgebase/        # Future: FAQ, SearchService
\\\

**Communication Patterns:**
- **Synchronous:** Controller → Service → Repository (core operations)
- **Event-Driven:** TicketCreated → AuditService, NotificationService (side effects)
- **Decoupling:** Modules only communicate via events, no direct dependencies

**Suitability for Project:**
- ✅ Logical modules avoid Maven multi-module complexity
- ✅ Event-driven architecture for extensibility (add new listeners without touching core)
- ✅ Flyway per-module migrations critical for schema evolution
- ✅ Runtime verification catches accidental coupling early
- ✅ Perfect for AI-assisted development (clear boundaries, declarative events)

**Risks:**
- ⚠️ RC1 stability - mitigated by GA November 21, 2025 (2 weeks)
- ⚠️ Newer library with less production battle-testing - mitigated by Spring team backing

**Upgrade Path:**
1. Develop with 2.0.0-RC1 (Nov 5-20)
2. Upgrade to 2.0 GA immediately (Nov 21)
3. Stabilize and test (Dec 2025 - Feb 2026)
4. Production deployment (March 2026)

**Verdict:** **Recommended** - GA imminent, features critical for modular architecture, low migration risk.

---

### 3.4 PostgreSQL 17.6

**Overview:**
PostgreSQL 17 is the latest stable major version, offering performance improvements and JSON enhancements.

**Key Features (PostgreSQL 17.x):**
- **Parallel Vacuuming:** Improved maintenance performance
- **JSON Enhancements:** Better indexing and querying of JSON columns
- **Performance:** Incremental improvements over 16.x
- **Logical Replication:** Enhanced streaming replication for HA

**Suitability for Project:**
- ✅ ACID guarantees for ticket assignment + audit logging in single transaction
- ✅ JSON columns for flexible audit event payloads
- ✅ Full-text search for ticket content and future knowledge base
- ✅ Proven Spring Data JPA integration
- ✅ 400 users well within performance limits (vertical scaling)

**Schema Design:**
- **users:** id, username, password_hash, role, email, department, location
- **tickets:** id, title, description, category, status, priority, created_by, assigned_to, created_at, updated_at
- **audit_logs:** id, event_type, user_id, entity_type, entity_id, payload (JSONB), timestamp
- **notifications:** id, user_id, ticket_id, channel (telegram/viber), status, sent_at

**Risks:**
- ⚠️ Single point of failure - mitigated by backup strategy (daily dumps + WAL archiving)
- 🔜 High availability: PostgreSQL replication (Phase 2 if uptime requirements increase)

**Verdict:** **Recommended** - Production-stable, battle-tested, perfect fit for transactional workload.

---

### 3.5 Spring Security 6.x

**Overview:**
Spring Security 6 provides authentication, authorization, and protection against common security vulnerabilities.

**Key Features:**
- **RBAC:** Role-based access control (ROLE_EMPLOYEE, ROLE_SUPPORT, ROLE_ADMIN)
- **Method Security:** @PreAuthorize annotations for fine-grained authorization
- **OAuth2/OIDC:** Client and resource server support for future Keycloak integration
- **Password Encoding:** BCrypt, Argon2 support
- **Session Management:** Configurable session strategies

**Security Design:**
- **Authentication:** Username/password, BCrypt hashing, session-based
- **Authorization:**
  - RBAC: 3 roles with hierarchical permissions
  - ABAC: Ownership checks (@PreAuthorize("@ticketSecurity.canEdit(#id, principal)"))
- **Audit:** All security events logged to audit_logs table
- **GDPR:** Employee data only (not patient data), retention policies configurable

**Suitability for Project:**
- ✅ Sufficient for 400 users, 3 roles
- ✅ OAuth2/OIDC interface allows future Keycloak migration without rewrite
- ✅ Spring Security 6.x compatible with Boot 4.0
- ✅ Solo developer can implement and maintain

**Risks:**
- 🔜 Keycloak migration complexity deferred - acceptable for Phase 1
- ⚠️ No SSO initially - acceptable for single application

**Verdict:** **Recommended** - Right-sized for Phase 1, migration path for Phase 2.

---

## 4. Comparative Analysis

### 4.1 Java 21 LTS vs Java 17 LTS

| Criterion | Java 21 LTS | Java 17 LTS |
|-----------|-------------|-------------|
| **LTS Support** | Until Sept 2029 (4 years) | Until Sept 2027 (2 years) |
| **Spring Boot 4.0** | Required (minimum Java 21) | Not supported |
| **Virtual Threads** | Yes (Project Loom) | No |
| **Pattern Matching** | Enhanced (record patterns) | Basic |
| **Records** | Enhanced | Basic |
| **Maturity** | 2 years old | 4 years old |

**Recommendation:** Java 21 LTS - Required by Spring Boot 4.0, longer support, modern features.

---

### 4.2 Spring Modulith 2.0 RC1 vs 1.4.4 Stable

| Criterion | 2.0.0-RC1 | 1.4.4 Stable |
|-----------|-----------|--------------|
| **Spring Boot** | 4.0.0-RC1 | 3.3.x |
| **Flyway Module Migrations** | Yes | No |
| **Runtime Verification** | Enhanced | Basic |
| **Event Publication** | JPA/JDBC/Mongo/Neo4j | JPA/JDBC only |
| **GA Release** | Nov 21, 2025 (2 weeks) | Already GA |
| **Production Stability** | RC1 (pre-release) | Stable |

**Recommendation:** 2.0.0-RC1 - GA imminent, Flyway module migrations critical, aligned with Boot 4.0.

---

### 4.3 PostgreSQL 17.6 vs 18.0

| Criterion | PostgreSQL 17.6 | PostgreSQL 18.0 |
|-----------|-----------------|-----------------|
| **Release Date** | Nov 2025 (patch) | Sept 2025 (major) |
| **I/O Performance** | Standard | 3× improvement |
| **UUIDv7() Function** | No | Yes |
| **Production Maturity** | High (17.x stable since Sept 2024) | Medium (2 months old) |
| **Risk** | Low | Medium |

**Recommendation:** PostgreSQL 17.6 - Conservative choice, 3× I/O not critical for 400 users, lower risk.

---

## 5. Trade-offs and Risks

### 5.1 Cutting-Edge Stack (Boot 4.0 + Modulith 2.0 RC1)

**Pros:**
- ✅ Latest ecosystem features (Framework 7.0, Jakarta EE 11, Java 21 baseline)
- ✅ Flyway module migrations enable true modular schema evolution
- ✅ Enhanced observability and virtual thread support
- ✅ Future-proof for 2026-2029 timeframe
- ✅ AI-assisted development benefits from modern, declarative patterns

**Cons:**
- ⚠️ RC1 stability risk - potential minor bugs before GA
- ⚠️ Less Stack Overflow content vs stable 3.x versions
- ⚠️ Dependency ecosystem may lag (some libraries not yet compatible)

**Mitigation:**
- 🛡️ GA release in 2 weeks (November 21, 2025)
- 🛡️ Comprehensive testing during December 2025 - February 2026
- 🛡️ AI assistance (Copilot) compensates for less online content
- 🛡️ Spring team backing ensures quick bug fixes post-GA

**Risk Level:** **Medium** - Acceptable for Q1 2026 production deployment.

---

### 5.2 Modular Monolith vs Microservices

**Pros (Monolith):**
- ✅ Single deployment unit - simpler DevOps for solo developer
- ✅ Faster development - no network overhead, shared transactions
- ✅ Sufficient for 400 users - no scalability pressure
- ✅ Easier debugging - single codebase, single process

**Cons (Monolith):**
- ⚠️ Scaling entire application together - acceptable for current scale
- ⚠️ Module boundaries enforced by Spring Modulith, not physical separation

**Trade-off:** Monolith with modular design allows future microservices extraction if needed. Spring Modulith provides logical boundaries that can become physical boundaries.

**Risk Level:** **Low** - Appropriate architectural choice for scale and team size.

---

### 5.3 Spring Security vs Keycloak

**Pros (Spring Security):**
- ✅ Simpler to implement and maintain for solo developer
- ✅ Sufficient for 3 roles, 400 users
- ✅ OAuth2/OIDC interface allows future Keycloak migration
- ✅ No additional infrastructure to manage

**Cons (Spring Security):**
- ⚠️ No SSO capability initially
- ⚠️ No LDAP/Active Directory integration out-of-box
- ⚠️ Manual user management (acceptable for Phase 1)

**Trade-off:** Start simple (Spring Security), migrate to Keycloak when SSO or AD integration required (likely Phase 2, 6-12 months post-launch).

**Risk Level:** **Low** - OAuth2/OIDC interface ensures low migration friction.

---

## 6. Recommendations

### 6.1 Proceed with Selected Stack

**Recommendation:** Move forward with the following technology stack:

| Component | Version | Rationale |
|-----------|---------|-----------|
| **Java** | 21 LTS | Production-safe LTS, modern features, required by Boot 4.0 |
| **Spring Boot** | 4.0.0-RC1 | Aligned with Modulith 2.0, GA expected Nov 2025 |
| **Spring Modulith** | 2.0.0-RC1 | Flyway migrations critical, GA Nov 21, 2025 |
| **PostgreSQL** | 17.6 | Conservative, ACID guarantees, proven Spring integration |
| **Spring Security** | 6.x | Right-sized for Phase 1, migration path to Keycloak |

**Timeline:**
1. **Nov 5-20, 2025:** Develop with RC1 versions
2. **Nov 21, 2025:** Upgrade to Spring Modulith 2.0 GA immediately
3. **Dec 2025 - Feb 2026:** Full testing and stabilization on GA
4. **March 2026:** Production deployment to ЦРБ

---

### 6.2 Upgrade Strategy

**Spring Modulith 2.0 RC1 → GA:**
- **Nov 21, 2025:** Upgrade dependencies in pom.xml
- **Nov 21-25:** Regression testing, verify no breaking changes
- **Nov 25-Dec 15:** Integration testing with Testcontainers
- **Dec 15 - Feb 28:** User acceptance testing (UAT) with ЦРБ stakeholders

**Spring Boot 4.0 RC1 → GA:**
- Expected GA: November 2025
- Same upgrade process as Spring Modulith

---

### 6.3 Risk Mitigation Plan

**Risk 1: Spring Modulith 2.0 RC1 Instability**
- **Likelihood:** Low (RC1 indicates near-GA stability)
- **Impact:** Medium (potential bugs delay development)
- **Mitigation:**
  - Monitor Spring Modulith GitHub issues
  - Upgrade to GA immediately on Nov 21
  - Comprehensive integration tests with Testcontainers
  - Delay production deployment until February 2026 if critical bugs found

**Risk 2: Dependency Compatibility Issues**
- **Likelihood:** Medium (newer Boot 4.0 ecosystem)
- **Impact:** Medium (may need to wait for library updates)
- **Mitigation:**
  - Validate all dependencies during architecture phase (Week 1)
  - Use Spring-managed dependencies (Spring Boot BOM)
  - Prefer Spring ecosystem libraries (Spring Data, Spring Security)

**Risk 3: PostgreSQL Single Point of Failure**
- **Likelihood:** Medium (no HA in Phase 1)
- **Impact:** High (system downtime during database failure)
- **Mitigation:**
  - Daily automated backups (pg_dump + WAL archiving)
  - 4-hour RTO acceptable for medical facility support system
  - Phase 2: Implement PostgreSQL replication if uptime requirements increase

---

### 6.4 Alternative Approaches

**If Risk Tolerance is Lower:**

**Conservative Stack (Stable Versions):**
| Component | Version | Trade-off |
|-----------|---------|-----------|
| Java | 21 LTS | Same (required by project goals) |
| Spring Boot | 3.5.7 | Stable but lacks Modulith 2.0 features |
| Spring Modulith | 1.4.4 | Stable but no Flyway module migrations |
| PostgreSQL | 17.6 | Same |
| Spring Security | 6.x | Same |

**Impact:** No Flyway per-module migrations - must manage schema evolution manually across modules. Less elegant but functional.

**Verdict:** Not recommended - Flyway module migrations are critical architectural feature worth 2-week RC1 risk.

---

## 7. Next Steps

### 7.1 Immediate Actions (Week 1)

1. **Create PRD (Product Requirements Document)**
   - Detailed functional requirements for all 6 modules
   - User stories for Employee, Support, Admin roles
   - API specifications (endpoints, request/response schemas)
   - Acceptance criteria

2. **Validate Dependencies**
   - Create Maven pom.xml with Spring Boot 4.0.0-RC1 BOM
   - Verify all required dependencies available (Spring Modulith, PostgreSQL driver, Testcontainers)
   - Test basic Spring Boot application startup

3. **Setup Development Environment**
   - Docker Compose for PostgreSQL 17.6
   - IntelliJ IDEA / VS Code with Spring Boot support
   - GitHub repository initialization

---

### 7.2 Architecture Phase (Week 2-3)

4. **Create Architecture Document**
   - Module structure (usermanagement, ticketing, audit, etc.)
   - Bounded contexts and ubiquitous language (DDD)
   - Event-driven communication patterns
   - Database schema design (ERD)

5. **Create Security Architecture**
   - Authentication flow (username/password, sessions)
   - Authorization matrix (role permissions)
   - Audit logging design
   - GDPR compliance measures

6. **Create DevOps Strategy**
   - Docker Compose for local development
   - GitHub Actions CI/CD pipeline
   - Backup and recovery procedures
   - Monitoring stack (Micrometer + Prometheus + Grafana)

7. **Create Test Strategy**
   - TDD workflow for AI-assisted development
   - Unit tests (JUnit 5 + Mockito), target 80%+ coverage
   - Integration tests (Testcontainers for PostgreSQL)
   - E2E tests (Playwright or Selenium)

---

### 7.3 Implementation Phase (Week 4+)

8. **Sprint Planning**
   - Break PRD into user stories and tasks
   - Prioritize features (MVP: ticket creation, assignment, resolution)
   - Estimate effort (story points or hours)

9. **Begin Development**
   - Module 1: User Management (users, roles, authentication)
   - Module 2: Ticketing (ticket CRUD, assignment logic)
   - Module 3: Audit (event listeners, immutable logs)
   - Module 4: Notifications (Telegram integration)
   - Module 5: Analytics (basic metrics)
   - Module 6: Knowledge Base (stub for future)

---

## 8. Appendices

### 8.1 Technology Version Matrix

| Technology | Version | Release Date | EOL/Support Until |
|------------|---------|--------------|-------------------|
| Java | 21 LTS | Sept 19, 2023 | Sept 2029 |
| Spring Boot | 4.0.0-RC1 | Oct 2025 | TBD (GA Nov 2025) |
| Spring Framework | 7.0.0-RC1 | Oct 2025 | TBD (GA Nov 2025) |
| Spring Modulith | 2.0.0-RC1 | Oct 2025 | GA Nov 21, 2025 |
| PostgreSQL | 17.6 | Nov 2025 | Sept 2029 |
| Spring Security | 6.x | 2023 | Active |
| Maven | 3.9+ | 2023 | Active |
| Docker | Latest | N/A | N/A |

### 8.2 Reference Links

**Official Documentation:**
- Java 21: https://openjdk.org/projects/jdk/21/
- Spring Boot 4.0: https://spring.io/projects/spring-boot
- Spring Modulith 2.0: https://github.com/spring-projects/spring-modulith
- PostgreSQL 17: https://www.postgresql.org/docs/17/
- Spring Security 6: https://spring.io/projects/spring-security

**GitHub Repositories:**
- Spring Modulith: https://github.com/spring-projects/spring-modulith
- Spring Boot: https://github.com/spring-projects/spring-boot

### 8.3 Glossary

- **LTS (Long-Term Support):** Extended support and security updates for production systems
- **RC (Release Candidate):** Pre-release version near-final, suitable for testing
- **GA (General Availability):** Official stable release for production use
- **ACID:** Atomicity, Consistency, Isolation, Durability (database transaction properties)
- **RBAC:** Role-Based Access Control
- **ABAC:** Attribute-Based Access Control
- **DDD:** Domain-Driven Design
- **TDD:** Test-Driven Development
- **CI/CD:** Continuous Integration / Continuous Deployment
- **SLA:** Service Level Agreement
- **RTO:** Recovery Time Objective (max downtime)
- **RPO:** Recovery Point Objective (max data loss)

---

## 9. Sign-off

**Prepared by:** Business Analyst Mary
**Date:** November 5, 2025
**Status:** Approved for Architecture Phase

**Next Workflow:** PRD (Product Requirements Document)
**Next Agent:** Business Analyst Mary (continue) or Product Manager (handoff)

**Stakeholder Approval:**
- [ ] Technical Lead: Nag
- [ ] Project Sponsor: ЦРБ Марьина Горка IT Department

---

_This research validates the technology foundation for the Tech-Support helpdesk system. The selected stack balances cutting-edge features with production stability, providing a solid foundation for AI-assisted development and long-term maintainability._
