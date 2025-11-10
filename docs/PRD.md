# Tech-Support - Product Requirements Document

**Author:** Nag (Business Analyst Mary)  
**Date:** 2025-11-05  
**Version:** 1.2 (Consolidated)

---

## Executive Summary

Tech-Support - это modern helpdesk system для ЦРБ Марьина Горка, разработанная с использованием AI-first подхода и современных архитектурных паттернов (Spring Modulith, DDD, Event-Driven Architecture).

### What Makes This Special

**Product Magic:** Modular Monolith Architecture позволяет начать с simple deployment (single server) и при необходимости migrated to microservices без полной переработки. Event-driven communication between modules обеспечивает loose coupling и тестируемость. Система сочетает простоту использования для сотрудников с powerful admin tools и compliance-ready audit trails.

---

## Project Classification

**Technical Type:** Backend Web Application + REST API  
**Domain:** Healthcare IT Support  
**Complexity:** Level 2 (Moderate - CRUD + business logic + multi-module architecture)

---

## Success Criteria

### Business Metrics

1. ✅ **>80% adoption rate** - 80%+ сотрудников ЦРБ создали хотя бы 1 тикет в первые 3 месяца
2. ✅ **<4h first response** - 90%+ тикетов получают первый ответ support agent в течение 4 часов
3. ✅ **<24h resolution (P2)** - 80%+ тикетов P2 (High priority) разрешаются за 24 часа
4. ✅ **<4 hour RTO** (Recovery Time Objective) при сбоях

### User Satisfaction

- ✅ **>80% сотрудников** оценивают систему как "полезную" или "очень полезную" (quarterly survey)

---

## Scalability & Quality Snapshot

> Детальные нефункциональные требования приведены в разделе [Non-Functional Requirements](#non-functional-requirements).

- **Масштабирование:** Платформа рассчитана на 400 активных пользователей и до 50 одновременных сессий. Архитектура stateless (сессии в Redis) и готовность Nginx upstream позволяют добавить горизонтальные инстансы без переработки кода; триггеры масштабирования описаны в NFR.
- **AI-first практики:** Java Records, модульные события и стандартизированные конфигурации подобраны так, чтобы ускорять генерацию кода и ревью с помощью LLM, сохраняя совместимость со Spring Modulith boundary tests.
- **Инженерная дисциплина:** CI/CD на GitHub Actions, 80%+ покрытия сервисного слоя, TDD и Sonar quality gate закреплены как обязательные условия, см. разделы Maintainability и Operational.
- **Наблюдаемость и логи:** Micrometer + Prometheus + Grafana, структурированные JSON-логи с маскированием PII, ротацией и регламентными проверками (детали — NFR/Reliability и NFR/Operational).
- **UX и обучение:** Desktop-first интерфейс, WCAG 2.1 AA, адаптивность под планшеты, пользовательские гайды и админ-руководства; квартальные опросы удерживают ≥80% положительных оценок.

---
## Scope
### MVP - Minimum Viable Product (March 2026)
#### MVP Must-Haves for Production Go-Live
##### MVP Module 1: Ticketing (Core)
1. ✅ Создание тикета (Employee role)
1. ✅ Просмотр и фильтрация тикетов (All roles)
1. ✅ Назначение тикета на support agent (Support, Admin roles)
1. ✅ Добавление комментариев к тикету (Support, Admin roles)
1. ✅ Обновление статуса тикета (Support, Admin roles)
1. ✅ Закрытие тикета с resolution notes (Support, Admin roles)
1. ✅ История изменений тикета (All roles)
1. ✅ Pagination и search для списка тикетов (20/page default, 100/page max)
##### MVP Module 2: User Management
1. ✅ Самостоятельная регистрация с email verification
1. ✅ Одобрение регистрации админом (Admin role)
1. ✅ 3 роли: Employee, Support, Admin (RBAC)
1. ✅ Профиль пользователя (имя, email, отдел, локация)
1. ✅ Смена пароля (self-service)
##### MVP Module 3: Audit Logging
1. ✅ Immutable audit log всех действий (create, update, delete, access)
1. ✅ Просмотр audit trail для конкретного тикета (Support, Admin roles)
1. ✅ GDPR compliance - retention policies, data sanitization
##### MVP Module 4: Notifications
1. ✅ Telegram уведомления о создании/назначении/обновлении тикета
1. ✅ Email fallback для критических уведомлений (если Telegram не доступен)
1. ✅ Настройки уведомлений (пользователь выбирает, что получать)
##### MVP Module 5: Analytics (Basic)
1. ✅ Dashboard: ticket volume by category, status, priority
1. ✅ Dashboard: average resolution time by category
1. ✅ Dashboard: support agent workload (assigned tickets count)
1. ✅ Export reports в CSV
##### MVP Module 6: Knowledge Base (Stub)
1. ✅ Placeholder module с интерфейсом (для future implementation)
### Growth Features (Post-MVP, Q2-Q3 2026)
📅 Phase 2 (3-6 months after MVP):
Enhanced Ticketing:
- File attachments (screenshots, logs) - max 10MB/file, 5 files/ticket
- Ticket templates для common issues
- Bulk actions (mass assignment, status update)
- Custom fields для specific categories
Advanced Notifications:
- Viber интеграция (в дополнение к Telegram)
- SMS alerts для P1 critical issues
- In-app notifications (real-time SSE)
Analytics & Reporting:
- SLA tracking с автоматической escalation
- Grafana dashboards для real-time metrics
- Predictive analytics - forecast ticket volume
- Customer satisfaction (CSAT) surveys после закрытия тикета
Knowledge Base (Full Implementation):
- FAQ с категоризацией и search
- Rich text editor для статей
- User voting (helpful/not helpful)
- AI-suggested solutions based на ticket description
### Vision Features (2027+)
🚀 Future Roadmap:
AI-Powered Features:
- ML-based ticket routing (automatic assignment to best agent)
- Predictive escalation (identify potential SLA breaches early)
- NLP-based search (semantic search across tickets and KB)
- Chatbot для initial triage
Collaboration:
- Real-time collaboration (WebSocket для multiple agents working on one ticket)
- Internal knowledge sharing (agents tag solutions for KB)
- Ticket merge/split для complex issues
Enterprise Integration:
- Keycloak SSO integration
- Active Directory sync для user management
- Integration с monitoring systems (auto-create tickets from alerts)

- REST API для third-party integrations

Multi-Facility:

- Multi-tenant architecture (separate data per facility)

- Cross-facility reporting
- Shared knowledge base across facilities
---
## Epic Breakdown
### MVP Epics (March 2026)
Epic 1: User Management Foundation
- Goal: Establish authentication и authorization infrastructure
- Stories: 5 stories (User registration, Email verification, Admin approval, Login, Password management)
- Estimated Duration: Week 1-2

- Delivers: Complete user onboarding flow with RBAC

Epic 2: Core Ticketing
- Goal: Enable employees to create и track tickets
- Stories: 7 stories (Create ticket, View tickets, Search/filter, Ticket details, Comments, Status updates, Close ticket)
- Estimated Duration: Week 3-5
- Delivers: End-to-end ticket lifecycle management
Epic 3: Support Workflows
- Goal: Enable support agents to manage workload
- Stories: 4 stories (Ticket assignment, Self-assignment, Workload dashboard, Bulk operations)
- Estimated Duration: Week 6-7
- Delivers: Efficient ticket distribution and tracking
Epic 4: Audit & Compliance
- Goal: Complete GDPR-compliant audit trail
- Stories: 3 stories (Immutable audit log, Ticket history view, Admin audit reports)
- Estimated Duration: Week 8
- Delivers: Full compliance with audit requirements
Epic 5: Notifications
- Goal: Real-time alerting via Telegram/Email
- Stories: 4 stories (Telegram integration, Email fallback, Notification preferences, Retry logic)
- Estimated Duration: Week 9-10
- Delivers: Reliable multi-channel notifications
Epic 6: Analytics (Basic)
- Goal: Provide visibility into support operations
- Stories: 4 stories (Ticket volume dashboard, Resolution time metrics, Agent workload, CSV export)
- Estimated Duration: Week 11-12
- Delivers: Data-driven insights for management
Total MVP: 27 stories, 12 weeks estimated
For detailed story breakdown, see [epics.md](./epics.md) _(to be created)_
---
## Functional Requirements
### Module 1: Ticketing
#### FR-1.1: Create Ticket (Employee Role)
User Story:
> As an **Employee**, I want to **create a support ticket** so that **I can report an IT issue and get help from the support team**.
Acceptance Criteria:
- ✅ Employee fills in: title (required, 5-200 chars), description (required, 10-5000 chars), category (dropdown), priority (P1/P2/P3)
- ✅ System auto-assigns: ticket ID (UUID), created_by (current user), created_at (timestamp), status=OPEN
- ✅ Ticket is immediately visible in employee's "My Tickets" list
- ✅ System sends Telegram notification to assigned support group (если category имеет default assignment)
- ✅ System creates AuditLog entry: event_type=TICKET_CREATED
Business Rules:
- Priority defaults to P3 (Low) if not selected
- Category is required (Hardware, Software, Network, Access, Other)
- Ticket auto-assigns to default support group based on category
Edge Cases:
- ❌ Employee cannot create ticket if account is suspended
- ⚠️ If Telegram notification fails → fallback to email
- ⚠️ If description contains PII (email, phone) → sanitize in logs
---
#### FR-1.2: View and Filter Tickets (All Roles)
User Story:
> As a **User (any role)**, I want to **view and filter tickets** so that **I can find specific tickets quickly**.
Acceptance Criteria:
- ✅ Employee sees: own created tickets only (unless Admin)
- ✅ Support sees: assigned tickets + unassigned tickets in their categories
- ✅ Admin sees: all tickets
- ✅ Filters: status (Open/InProgress/Resolved/Closed), category, priority, assignee, date range
- ✅ Search: full-text search across title, description (PostgreSQL full-text search)
- ✅ Sorting: by created_at, updated_at, priority (asc/desc)
- ✅ Pagination: 20 items/page default, max 100/page (query param ?page=1&size=20)
Business Rules:
- Default sort: created_at DESC (newest first)
- Search indexes: title, description (PostgreSQL GIN index)
- Performance: <500ms for search on 50K tickets
API Specification:
GET /api/v1/tickets?status=OPEN&category=HARDWARE&page=1&size=20&sort=created_at,desc
Response: 200 OK
{
  "content": [
    {
      "id": "uuid",
      "title": "string",
      "status": "OPEN",
      "category": "HARDWARE",
      "priority": "P2",
      "created_at": "2025-11-05T10:30:00Z",
      "assigned_to": "uuid or null"
    }
  ],
  "page": 1,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8
}
---
#### FR-1.3: Assign Ticket (Support, Admin Roles)
User Story:
> As a **Support Agent**, I want to **assign a ticket to myself or another agent** so that **ownership is clear and work is distributed**.
Acceptance Criteria:
- ✅ Support can assign: unassigned tickets OR tickets assigned to themselves
- ✅ Admin can assign: any ticket to any support agent
- ✅ Assignee must have role=SUPPORT or role=ADMIN
- ✅ Assignment updates: assigned_to (user_id), assigned_at (timestamp), status=IN_PROGRESS (if was OPEN)
- ✅ System sends Telegram notification to new assignee
- ✅ System creates AuditLog entry: event_type=TICKET_ASSIGNED, old_value, new_value
Business Rules:
- Self-assignment: Support agents can claim unassigned tickets (click "Assign to Me" button)
- Reassignment: Admin can reassign from one agent to another
- Ownership check: @PreAuthorize("@ticketSecurity.canAssign(#ticketId, principal)")
Edge Cases:
- ❌ Employee cannot assign tickets (403 Forbidden)
- ⚠️ If assignee account is suspended → validation error
- ⚠️ Reassignment notifies both old and new assignee
---
#### FR-1.4: Add Comment (Support, Admin Roles)
User Story:
> As a **Support Agent**, I want to **add comments to a ticket** so that **I can communicate progress and ask for more details**.
Acceptance Criteria:
- ✅ Comment fields: text (required, 5-2000 chars), created_by (current user), created_at (timestamp)
- ✅ Comments are immutable (cannot edit/delete after creation)
- ✅ Comments appear in chronological order on ticket detail page
- ✅ System sends Telegram notification to ticket creator (Employee)
- ✅ System creates AuditLog entry: event_type=COMMENT_ADDED
Business Rules:
- Only Support and Admin can add comments
- Employee can view comments but not add (MVP scope - может быть расширено в Phase 2)
API Specification:
POST /api/v1/tickets/{ticketId}/comments
Request: {
  "text": "Please provide your computer's serial number."
}
Response: 201 Created
{
  "id": "uuid",
  "ticketId": "uuid",
  "text": "Please provide your computer's serial number.",
  "created_by": "uuid",
  "created_at": "2025-11-05T11:00:00Z"
}
---
#### FR-1.5: Update Status (Support, Admin Roles)
User Story:
> As a **Support Agent**, I want to **update ticket status** so that **progress is visible to everyone**.
Acceptance Criteria:
- ✅ Status transitions: OPEN → IN_PROGRESS → RESOLVED → CLOSED
- ✅ Status can skip steps (e.g., OPEN → RESOLVED directly if simple issue)
- ✅ RESOLVED requires resolution_notes (required, 10-1000 chars)
- ✅ System sends Telegram notification to ticket creator
- ✅ System creates AuditLog entry: event_type=STATUS_CHANGED, old_value, new_value
Business Rules:
- Status validation: Cannot go from CLOSED back to OPEN (must create new ticket)
- Resolution notes: Required when moving to RESOLVED or CLOSED
- Ownership: Only assigned agent or Admin can update status
State Machine:
OPEN → IN_PROGRESS (when assigned)
IN_PROGRESS → RESOLVED (when fixed, requires resolution_notes)
RESOLVED → CLOSED (after 7 days or manual close)
RESOLVED → IN_PROGRESS (if issue reoccurs within 7 days)
---
#### FR-1.6: Close Ticket (Support, Admin Roles)
User Story:
> As a **Support Agent**, I want to **close a ticket** so that **it's marked as completed and removed from active work queue**.
Acceptance Criteria:
- ✅ Ticket must be in status=RESOLVED before closing
- ✅ Closing updates: status=CLOSED, closed_at (timestamp), closed_by (user_id)
- ✅ Closed tickets are excluded from default ticket list (require explicit filter)
- ✅ System sends Telegram notification to ticket creator
- ✅ System creates AuditLog entry: event_type=TICKET_CLOSED
Business Rules:
- Auto-close: Tickets in RESOLVED status auto-close after 7 days if no activity
- Reopen: Cannot reopen closed ticket (must create new ticket and reference old one)
---
#### FR-1.7: View Ticket History (All Roles)
User Story:
> As a **User**, I want to **view complete ticket history** so that **I can see all changes and understand what happened**.
Acceptance Criteria:
- ✅ History shows: all status changes, assignments, comments, resolution notes
- ✅ Each entry shows: action type, user who performed it, timestamp, old/new values
- ✅ History is read-only and immutable
- ✅ History loads via AuditLog module query
API Specification:
GET /api/v1/tickets/{ticketId}/history
Response: 200 OK
[
  {
    "event_type": "TICKET_CREATED",
    "timestamp": "2025-11-05T10:30:00Z",
    "user": { "id": "uuid", "name": "Иван Иванов" },
    "details": { "status": "OPEN", "priority": "P2" }
  },
  {
    "event_type": "TICKET_ASSIGNED",
    "timestamp": "2025-11-05T10:35:00Z",
    "user": { "id": "uuid", "name": "Петр Петров" },
    "details": { "old_assignee": null, "new_assignee": "uuid" }
  }
]
---
### Module 2: User Management

#### FR-2.1: Self-Registration (Public Access)

User Story:

> As a **New Employee**, I want to **register an account** so that **I can start creating support tickets**.

Acceptance Criteria:

- ✅ Registration form: email (unique, validated), password (min 12 chars, complexity rules), full_name, department, location
- ✅ Email verification: System sends confirmation link (expires in 24 hours)
- ✅ Account status: pending_approval after email verification
- ✅ Admin receives notification about new registration (Telegram + email)
- ✅ Password hashed: BCrypt rounds=12

Business Rules:

- Email must be unique (duplicate check before insert)
- Password policy: min 12 chars, uppercase + lowercase + digit + special char
- Default role: ROLE_EMPLOYEE (assigned after approval)

Password History:

- Store hash of last 5 passwords in separate password_history table
- Prevent password reuse

API Specification:

```http
POST /api/v1/auth/register
Request: {
  "email": "ivanov@crb.by",
  "password": "SecureP@ss123",
  "full_name": "Иван Иванов",
  "department": "Терапия",
  "location": "Корпус А"
}
Response: 201 Created
{
  "message": "Регистрация успешна. Проверьте email для подтверждения."
}
```

---

#### FR-2.2: Admin Approval (Admin Role)

User Story:

> As an **Admin**, I want to **approve or reject new registrations** so that **only authorized employees can access the system**.

Acceptance Criteria:

- ✅ Admin views pending registrations list
- ✅ Admin can: approve (activates account), reject (deletes account), request more info
- ✅ Approval updates: account status=active, role=ROLE_EMPLOYEE
- ✅ System sends email to user: approval or rejection notification
- ✅ System creates AuditLog entry: event_type=USER_APPROVED or USER_REJECTED

Business Rules:

- Only Admin can approve/reject
- Rejected registrations are soft-deleted (status=rejected, retention 90 days for audit)

---

#### FR-2.3: Role Management (Admin Role)

User Story:

> As an **Admin**, I want to **assign roles to users** so that **support agents have appropriate permissions**.

Acceptance Criteria:

- ✅ 3 roles: ROLE_EMPLOYEE, ROLE_SUPPORT, ROLE_ADMIN
- ✅ Role hierarchy: Admin > Support > Employee
- ✅ Admin can promote Employee → Support, Support → Admin
- ✅ Admin can demote Support → Employee, Admin → Support
- ✅ System creates AuditLog entry: event_type=ROLE_CHANGED, old_role, new_role

Business Rules:

- Admin cannot demote themselves (must be done by another Admin)
- At least one Admin must always exist (system validation)

Permission Matrix:

| Action | Employee | Support | Admin |
|--------|----------|---------|-------|
| Create ticket | ✅ | ✅ | ✅ |
| View own tickets | ✅ | ✅ | ✅ |
| View all tickets | ❌ | ✅ | ✅ |
| Assign ticket | ❌ | ✅ (self + unassigned) | ✅ (any) |
| Comment on ticket | ❌ | ✅ | ✅ |
| Update status | ❌ | ✅ (assigned to self) | ✅ (any) |
| Close ticket | ❌ | ✅ (assigned to self) | ✅ (any) |
| Manage users | ❌ | ❌ | ✅ |
| View audit logs | ❌ | ✅ (own actions) | ✅ (all) |
| Manage analytics | ❌ | ✅ (view only) | ✅ (view + export) |

---

#### FR-2.4: User Profile (All Roles)

User Story:

> As a **User**, I want to **view and edit my profile** so that **my contact information is up-to-date**.

Acceptance Criteria:

- ✅ Profile fields: full_name, email (read-only, cannot change), department, location, phone (optional)
- ✅ User can update: full_name, department, location, phone
- ✅ System creates AuditLog entry: event_type=PROFILE_UPDATED, changed_fields

Business Rules:

- Email cannot be changed (must contact Admin)
- Profile updates require current password confirmation (security)

---

#### FR-2.5: Password Change (All Roles)

User Story:

> As a **User**, I want to **change my password** so that **I can maintain account security**.

Acceptance Criteria:

- ✅ Password change requires: current_password (validation), new_password (complexity check)
- ✅ Password history: Cannot reuse last 5 passwords
- ✅ Session invalidation: All active sessions are revoked after password change
- ✅ System sends email notification about password change
- ✅ System creates AuditLog entry: event_type=PASSWORD_CHANGED

Business Rules:

- Account lockout: 5 failed password attempts → 15-minute lockout
- Password expiry: Passwords expire after 90 days (warning at 80 days)

---

### Module 3: Audit Logging
#### FR-3.1: Immutable Audit Log (System)
User Story:
> As a **System**, I want to **log all actions** so that **there's a complete audit trail for compliance**.
Acceptance Criteria:
- ✅ Every action creates AuditLog entry: event_type, user_id, entity_type, entity_id, payload (JSONB), timestamp
- ✅ Audit logs are append-only (no UPDATE or DELETE operations)
- ✅ Payload includes: old_value, new_value, action metadata
- ✅ PII in payload is sanitized (emails masked, passwords never logged)
Database Schema (audit_logs table):
```sql
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(50) NOT NULL,
    user_id UUID,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    payload JSONB NOT NULL,  -- REQUIRED: JSONB for flexible structure and GIN indexing
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_payload_gin ON audit_logs USING GIN(payload jsonb_path_ops);
```
JSONB Payload Structure Examples:
```json
// TICKET_ASSIGNED event
{
  "old_value": null,
  "new_value": {
    "assignee_id": "550e8400-e29b-41d4-a716-446655440000",
    "assignee_name": "Петр Петров"
  },
  "metadata": {
    "ip_address": "192.168.1.10",
    "user_agent": "Mozilla/5.0..."
  }
}
// STATUS_CHANGED event
{
  "old_value": {"status": "OPEN"},
  "new_value": {"status": "RESOLVED", "resolution_notes": "Принтер перезагружен"},
  "metadata": {"duration_seconds": 1800}
}
```
JSONB Query Examples (Performance):
- Find all assignments to specific user: `payload @> '{"new_value": {"assignee_id": "uuid"}}'`
- Find status changes: `payload -> 'new_value' ->> 'status' = 'RESOLVED'`
- GIN index ensures <50ms query time on 1M+ audit records
Event Types:
- TICKET_CREATED, TICKET_ASSIGNED, TICKET_UPDATED, STATUS_CHANGED, COMMENT_ADDED, TICKET_CLOSED
- USER_REGISTERED, USER_APPROVED, USER_REJECTED, ROLE_CHANGED, PROFILE_UPDATED, PASSWORD_CHANGED
- LOGIN_SUCCESS, LOGIN_FAILED, LOGOUT, SESSION_EXPIRED
Business Rules:
- Retention: 1 year hot storage (PostgreSQL), 5 years cold archive (S3 or filesystem)
- GDPR: Audit logs include employee data (not patient data), sanitized for privacy
- JSONB payload MUST NOT exceed 1MB (validation at application layer)
---
#### FR-3.2: View Audit Trail (Support, Admin Roles)
User Story:
> As an **Admin**, I want to **view audit trail for a ticket** so that **I can investigate issues or disputes**.
Acceptance Criteria:
- ✅ Audit trail filtered by: entity_type=TICKET, entity_id={ticketId}
- ✅ Shows: all events related to that ticket (creation, updates, comments, assignments)
- ✅ Formatted for readability: "Петр Петров assigned ticket to Иван Иванов at 10:35"
API Specification:
GET /api/v1/audit/tickets/{ticketId}
Response: 200 OK
[
  {
    "timestamp": "2025-11-05T10:30:00Z",
    "event_type": "TICKET_CREATED",
    "user": "Иван Иванов",
    "details": "Created ticket #12345: 'Printer not working'"
  },
  {
    "timestamp": "2025-11-05T10:35:00Z",
    "event_type": "TICKET_ASSIGNED",
    "user": "Петр Петров",
    "details": "Assigned ticket to Иван Иванов"
  }
]
---
### Module 4: Notifications
#### FR-4.1: Telegram Notifications (System)
User Story:
> As a **User**, I want to **receive Telegram notifications** so that **I'm immediately aware of important ticket updates**.
Acceptance Criteria:
- ✅ Notification triggers: Ticket created (to support group), Ticket assigned (to assignee), Comment added (to creator), Status changed (to creator)
- ✅ Message format: "🎫 Ticket #12345: Printer not working | Status: OPEN | Assigned to: You | View: [link]"
- ✅ Deep link: Opens ticket detail page in web app
- ✅ User preferences: Can enable/disable notifications per event type
Business Rules:
- Telegram Bot API integration
- Rate limiting: Max 20 messages/user/hour (prevent spam)
- Fallback: If Telegram fails → email notification
---
#### FR-4.2: Email Fallback (System)
User Story:
> As a **User**, I want to **receive email notifications** so that **I'm notified even if Telegram is unavailable**.
Acceptance Criteria:
- ✅ Email sent if: Telegram fails OR user has no Telegram configured OR critical P1 issue
- ✅ Email template: Subject, body with ticket details, link to web app
- ✅ SMTP configuration: Configurable via environment variables
---
#### FR-4.3: Notification Preferences (All Roles)
User Story:
> As a **User**, I want to **configure notification preferences** so that **I only receive relevant alerts**.
Acceptance Criteria:
- ✅ Preferences: Enable/disable per event type (ticket created, assigned, commented, status changed)
- ✅ Preferences: Choose channel (Telegram, Email, Both)
- ✅ Defaults: All notifications enabled for new users
---
### Module 5: Analytics (Basic)
#### FR-5.1: Dashboard - Ticket Volume (Support, Admin Roles)
User Story:
> As an **Admin**, I want to **view ticket volume metrics** so that **I can understand workload trends**.
Acceptance Criteria:
- ✅ Charts: Ticket volume by category (pie chart), by status (bar chart), by priority (bar chart)
- ✅ Time range filter: Last 7 days, Last 30 days, Last 90 days, Custom date range
- ✅ Real-time data: Refreshes every 5 minutes
---
#### FR-5.2: Dashboard - Resolution Time (Support, Admin Roles)
User Story:
> As an **Admin**, I want to **view average resolution time** so that **I can identify bottlenecks**.
Acceptance Criteria:
- ✅ Metrics: Average time-to-resolution by category (bar chart)
- ✅ Calculation: closed_at - created_at (business hours only, excludes weekends)
- ✅ Goal indicator: Show if below/above SLA target (24h for P2)
---
#### FR-5.3: Dashboard - Agent Workload (Admin Role)
User Story:
> As an **Admin**, I want to **view agent workload** so that **I can balance ticket assignments**.
Acceptance Criteria:
- ✅ Table: Agent name, assigned tickets count, avg resolution time
- ✅ Sorting: By assigned count, by resolution time
- ✅ Filter: Show only active agents (exclude suspended)
---
#### FR-5.4: Export Reports (Admin Role)
User Story:
> As an **Admin**, I want to **export reports to CSV** so that **I can analyze data in Excel**.
Acceptance Criteria:
- ✅ Export options: Ticket list (filtered), Agent performance, Category breakdown
- ✅ CSV format: UTF-8 encoding, comma-separated
- ✅ Download link: Generates file and provides download
---
### Module 6: Knowledge Base (Stub for Future)
#### FR-6.1: Placeholder Module
Acceptance Criteria:
- ✅ UI shows "Knowledge Base" menu item
- ✅ Clicking opens placeholder page: "Coming Soon - Self-Service FAQ"
- ✅ Backend has KnowledgeBasePort interface (for future implementation)
---
## Non-Functional Requirements

### Performance

#### Latency Targets (API Endpoints)

**Percentile Metrics (Critical SLAs):**

- **p50:** <200ms for API endpoints, <100ms for simple CRUD operations
- **p95:** <300ms for API endpoints, <1s for complex searches with pagination
- **p99:** <500ms for API endpoints

**Specific Endpoint Requirements:**

- Simple CRUD (ticket by ID, user profile): p95 <100ms
- Full-text search (50K tickets): p95 <500ms with pagination applied
- Dashboard analytics: p95 <2s with data aggregation

#### Throughput

- Sustain 50 concurrent users without breaching latency targets
- 500 daily active users peak with no degraded UX
- Ticket creation endpoint handles 100 requests/second sustained for 5 minutes

#### Database Performance

- Growth projection: 10K tickets/year, 5-year retention ≈50K records
- Indexed queries: p95 <100ms for standard queries
- JSONB containment queries: p95 <150ms with GIN indexes
- Audit log queries: <50ms p95 on 1M+ records with GIN indexing
Performance Testing Plan:
- Tooling: Apache JMeter + Gatling load suites committed to `tests/performance`
- Schedule: Week 2 (baseline), Week 8 (regression), Week 12 (pre-production)
- Success Criteria:
  - API suite meets latency targets above
  - Ticket creation spike test sustains 100 req/sec with <1% error rate
  - Search scenarios return <1s for 50K ticket dataset with pagination
---
### Reliability
Uptime:
- 99.5% uptime SLA (measured monthly, excludes planned maintenance)
- Planned maintenance windows: Saturday 2-4 AM (announced 48h in advance)
Recovery:
- RTO (Recovery Time Objective): <4 hours
- RPO (Recovery Point Objective): <1 hour (daily backups + WAL archiving)
Backup:
- Daily PostgreSQL dumps at 2 AM (automated via cron)
- WAL archiving for point-in-time recovery
- Backup verification: Weekly restore test to staging environment
- Retention: 30 days daily backups, 12 months monthly archives
Failover (Phase 2):
- PostgreSQL replication for high availability (if uptime requirements increase to 99.9%)
---
### Security
#### Authentication
- Username/password (BCrypt rounds=12, salt per user)
- Session-based authentication (30-day expiry, HttpOnly cookies)
- Password policy: Min 12 chars, uppercase + lowercase + digit + special char
- Password history: Last 5 passwords, prevent reuse
- Account lockout: 5 failed attempts → 15-minute lockout
#### Authorization
- RBAC: 3 roles (Employee, Support, Admin) with hierarchical permissions
- ABAC: Ownership-based checks (@PreAuthorize("@ticketSecurity.canEdit(#id, principal)"))
- Method security: @PreAuthorize annotations on all service methods
#### Data Protection
- TLS 1.3 for all HTTP traffic (enforced via Nginx reverse proxy)
- AES-256 at rest (PostgreSQL encryption via pg_crypto)
- PII sanitization in logs (emails masked, passwords never logged)
#### Audit
- 100% action logging (all CREATE, UPDATE, DELETE operations)
- GDPR compliant: Employee data only (not patient data)
- Retention: 1 year hot, 5 years cold archive
#### Incident Response Plan
- Detection: Real-time alerts via audit log monitoring and Prometheus rules
- Response Team: IT Admin (L1) + Developer Nag (L2) + ЦРБ IT Department Head (L3)
- Response SLA: Critical incidents acknowledged <1h, contained <4h; High severity acknowledged <4h, contained <24h; Medium severity acknowledged <8h, contained <3 days
- Notification Chain: Immediate escalation to ЦРБ management for data breach, PII exposure, ransomware, or downtime >4h
- Post-incident: Root-cause analysis <24h for Sev1, <5 days for Sev2; public report approved by ЦРБ management prior to closure
#### Session Management
- Session revocation: Admin can force-logout users
- All sessions invalidated on password change
- Session timeout: 30 days inactivity
#### Config and Secrets
- Environment variables for sensitive config (database passwords, API keys)
- Secret rotation: Every 90 days (documented procedure)
- Secrets stored in hospital-approved vault; deployment workloads retrieve short-lived tokens at runtime
---
### Scalability
Vertical Scaling (MVP):
- Sufficient for 400 users
- Single application server, single database server
Horizontal Readiness (Future):
- Stateless application design (sessions in Redis, not in-memory)
- Database connection pooling (HikariCP, max 50 connections)
- Load balancer ready (Nginx upstream config prepared)
Scaling Triggers:
- CPU >70% sustained 10 min → vertical scale (add vCPU/RAM)
- DB connections >80% pool → increase pool size or add read replica
- Storage >80% → expand disk or implement archival strategy
---
### Maintainability
Code Quality:
- Code coverage: 80%+ for service layer (measured via JaCoCo)
- TDD approach: Tests written before implementation
- Static analysis: SonarQube checks (no critical issues)
Documentation:
- API docs: SpringDoc OpenAPI (Swagger UI at /swagger-ui.html)
- Architecture diagrams: Module structure, database ERD, sequence diagrams
- README: Setup instructions, development workflow, deployment guide
- Runbooks: Incident response procedures, backup/restore procedures
Modularity:
- Spring Modulith logical modules with clear bounded contexts
- Event-driven decoupling between modules
- Runtime verification prevents boundary violations
Testing:
- Unit tests: JUnit 5 + Mockito (isolate dependencies)
- Integration tests: Testcontainers for PostgreSQL (real database)
- E2E tests: Playwright or Selenium (critical user flows)
---
### Usability
Responsive Design:
- Desktop-first (primary use case: office workstations)
- Mobile-friendly (responsive layout for tablet/phone)
Accessibility:
- WCAG 2.1 AA compliance (public sector requirement)
- Keyboard navigation support
- Screen reader compatible
Localization:
- Primary language: Russian
- Future: Belarusian (Phase 2)
User Training:
- Admin documentation: User management, system configuration
- User guides: How to create ticket, track ticket, use notifications
- Quarterly survey maintains ≥80% положительных оценок (см. Success Criteria)
---
### Operational
Deployment:
- Docker containers (application + PostgreSQL)
- Docker Compose for local/staging environments
- Production: On-premise ЦРБ infrastructure (Linux server)
Monitoring:
- Micrometer + Prometheus + Grafana stack
- Metrics: API response times, error rates, database query performance, JVM metrics
- Alerts: Slack/Telegram notifications for critical issues
Logging:
- Structured JSON logs (SLF4J + Logback)
- Format: timestamp, level, user_id, action, correlation_id
- Retention: 90 days hot logs, 1 year cold archive
- Sanitization: PII redacted (passwords, emails masked)
- ELK stack (optional Phase 2)
CI/CD:
- GitHub Actions for build, test, deploy pipeline
- Automated tests run on every commit
- Deployment to staging on merge to main branch
- Production deployment via manual approval
Backup Verification:
- Weekly restore test to staging (every Monday 10 AM)
- Automated smoke tests after restore
---
## API Specification Summary
Base URL: /api/v1
Versioning: URL-based (/api/v1, /api/v2 in future)
Authentication: Session-based (HttpOnly cookies)
Error Handling:

```json
{
  "timestamp": "2025-11-05T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "title",
      "message": "Title must be between 5 and 200 characters"
    }
  ]
}
```

Core Endpoints:
- POST /api/v1/auth/register - Self-registration
- POST /api/v1/auth/login - Login (creates session)
- POST /api/v1/auth/logout - Logout (invalidates session)
- GET /api/v1/tickets - List tickets (with filters, pagination)
- POST /api/v1/tickets - Create ticket
- GET /api/v1/tickets/{id} - Get ticket details
- PUT /api/v1/tickets/{id}/assign - Assign ticket
- POST /api/v1/tickets/{id}/comments - Add comment
- PUT /api/v1/tickets/{id}/status - Update status
- GET /api/v1/tickets/{id}/history - View audit trail
- GET /api/v1/users - List users (Admin only)
- PUT /api/v1/users/{id}/role - Change role (Admin only)
- GET /api/v1/analytics/dashboard - Dashboard metrics
- GET /api/v1/audit/tickets/{id} - Audit trail for ticket
---
## Technical Constraints
### Technology Stack
- Backend: Java 21 LTS, Spring Boot 4.0.0-RC2, Spring Modulith 2.0.0-RC2
- Database: PostgreSQL 17.6
  - **JSONB Required:** All flexible/nested data structures MUST use JSONB columns (not TEXT/JSON)
  - Use cases: Audit log payloads, notification preferences, event metadata, custom fields
  - Rationale: Native indexing (GIN), efficient querying, schema flexibility without migrations
- Testing: JUnit 5, Testcontainers, Mockito
- Build: Gradle 8.11.1
- Containerization: Docker, Docker Compose
- Version Control: Git, GitHub
| Spring Boot | 4.0.0-RC2 | Validated with Java 21, Modulith 2.0 RC2 |
| Spring Modulith | 2.0.0-RC2 | Runtime boundary verification enabled |
| Spring Security | 6.4.2 | Aligns with session-based auth model |
| SpringDoc OpenAPI | 2.7.0 | Supports Spring Boot 4.0 RC2 |
| Testcontainers | 1.20.4 | PostgreSQL 17 compatibility |
| Micrometer Core | 1.14.2 | Metrics emission for Prometheus stack |
| HikariCP | 6.2.1 | Connection pooling tuned for 50 concurrent users |
| Flyway Core | 11.1.0 | Modulith module-aware migrations |
| Jackson Databind | 2.18.2 | JSON serialization hardened (CVE triaged) |
| Logback Classic | 1.5.12 | Structured JSON logging |
| PostgreSQL JDBC | 42.7.4 | SCRAM-SHA-256 auth compliance |
### Infrastructure
- Deployment: On-premise server (ЦРБ infrastructure, Linux)
- CI/CD: GitHub Actions (cloud-based builds)
### Team
- Solo developer + AI-assisted development (GitHub Copilot, Claude)
- Experience: Strong Java, Spring Boot, PostgreSQL
- Learning goals: DDD, Spring Modulith, TDD practices
### Timeline
- Development: 3-4 months (Nov 2025 - Feb 2026)
- Feature freeze: Jan 1, 2026
- UAT: Feb 1-14, 2026
- Production deployment: March 10, 2026
---
## Integration Requirements
External Systems:
### Telegram Bot API
- Purpose: Real-time notifications
- Integration: REST API (sending messages)
- Fallback: Email if Telegram unavailable
### Viber Bot API (Phase 2)
- Purpose: Alternative notification channel
- Integration: REST API
### Email (SMTP)
- Purpose: Fallback notifications, email verification
- Integration: Spring Boot Mail (SMTP client)
- Configuration: Environment variables (smtp.host, smtp.port, smtp.username, smtp.password)
### OAuth2/OIDC Interface (Future)
- Purpose: Keycloak SSO integration (Phase 2/3)
- Integration: Spring Security OAuth2 Client
---
## Module Dependencies & Communication
Module Structure (Spring Modulith):
tech-support/
├── usermanagement/     # User, UserRole, Authentication
├── ticketing/          # Ticket, TicketCategory, TicketStatus
├── audit/              # AuditLog - immutable event store
├── analytics/          # TicketMetrics - aggregated views
├── notification/       # TelegramService, ViberService, EmailService
└── knowledgebase/      # FAQ (stub for future)
### Communication Patterns
- **Synchronous:** Controller → Service → Repository (core operations)
- **Event-Driven:** Spring Modulith ApplicationEvents для side-effects
  - TicketCreatedEvent → AuditService, NotificationService
  - TicketAssignedEvent → AuditService, NotificationService
  - UserApprovedEvent → AuditService, NotificationService
### Module Boundaries
- Modules communicate ONLY via events (no direct imports)
- Runtime verification enforces boundaries (Spring Modulith)
- Each module has separate database schema (Flyway per-module migrations)
### Database Schema Ownership
- usermanagement: users, roles, password_history tables
- ticketing: tickets, comments tables
- audit: audit_logs table (JSONB: payload column)
- analytics: ticket_metrics (materialized view)
- notification: notification_queue, notification_preferences tables (JSONB: preferences column)
### JSONB Usage Guidelines
#### Must Use JSONB For
1. **Audit Log Payloads** (`audit_logs.payload`): Event metadata, old/new values, context
2. **Notification Preferences** (`notification_preferences.preferences`): Per-user channel/event settings
3. **Event Metadata** (Spring Modulith event publication registry): Serialized event data
4. **Custom Fields** (Phase 2): Ticket/user extensible properties without schema migrations
5. **API Request/Response Logs** (if needed): HTTP headers, query params for debugging
#### Must Not Use JSONB For
1. **Primary Identifiers**: Use UUID/BIGINT (indexed, foreign key constraints)
2. **Fixed Schema Entities**: Use typed columns (VARCHAR, INTEGER, TIMESTAMPTZ) for predictable fields
   - Examples: `ticket.title`, `user.email`, `ticket.status` (use ENUM or VARCHAR)
3. **Frequently Filtered Fields**: Use typed columns for WHERE clauses (better query planner statistics)
4. **Foreign Keys**: Always use typed UUID/BIGINT columns (referential integrity)
#### JSONB Index Strategy
- **GIN Index (jsonb_path_ops)**: For containment queries (`@>`, `?`, `?&`, `?|`)
  - Example: `CREATE INDEX idx_audit_payload_gin ON audit_logs USING GIN(payload jsonb_path_ops);`
  - Use for: "Find all events where payload contains specific key/value"
- **Expression Index**: For frequently queried JSON paths
  - Example: `CREATE INDEX idx_notif_prefs_telegram ON notification_preferences((preferences->>'telegram_enabled'));`
  - Use for: "Find all users with Telegram enabled"
#### JSONB Validation
- **Application Layer**: Validate JSON structure before INSERT (use JSON Schema or custom validators)
- **Database Constraint**: `CHECK (jsonb_typeof(payload) = 'object')` ensures valid JSON object
- **Size Limit**: Max 1MB per JSONB column (prevent bloat, log warning if >100KB)
#### JSONB Performance Considerations
- **GIN Index Size**: ~30% overhead on disk, but 100x faster queries on large tables (1M+ rows)
- **Update Cost**: JSONB updates rewrite entire column (use typed columns for frequently updated fields)
- **Query Planning**: PostgreSQL optimizer understands JSONB GIN indexes (verified in PG 17.6)
---
## Risk Analysis & Mitigation
Technical Risks:
Risk 1: Spring Modulith 2.0 RC2 Instability
- Likelihood: Low (RC2 near-GA, GA Nov 21, 2025)
- Impact: Medium (potential bugs delay development)
- Mitigation:
  - Monitor GitHub issues
  - Upgrade to GA immediately on Nov 21
  - Comprehensive integration tests (Testcontainers)
  - Rollback plan to RC2 if GA introduces breaking changes
Risk 2: Dependency Compatibility (Spring Boot 4.0)
- Likelihood: Medium (newer ecosystem)
- Impact: Medium (may need library updates)
- Mitigation:
  - Validate all dependencies Week 1 (build.gradle verification)
  - Use Spring-managed dependencies (Boot BOM)
  - Verified: SpringDoc 2.7.0, Testcontainers 1.20.4, Micrometer 1.14.2 all compatible
Risk 3: PostgreSQL Single Point of Failure
- Likelihood: Medium (no HA in MVP)
- Impact: High (system downtime)
- Mitigation:
  - Daily backups + WAL archiving
  - 4-hour RTO acceptable for non-critical system
  - Phase 2: PostgreSQL replication if uptime requirements increase
Risk 4: Attachment Storage Growth
- Likelihood: Medium (10MB/file × 5 files/ticket × 10K tickets = ~500GB over 5 years)
- Impact: Medium (disk space exhaustion)
- Mitigation:
  - Filesystem storage with 1TB allocated
  - Monitor weekly, alert at 80%
  - Archive >2 years old to cold storage
  - Migrate to MinIO if >50GB or multi-server needed
Operational Risks:
Risk 5: Solo Developer Bottleneck
- Likelihood: High (single developer)
- Impact: High (delays if developer unavailable)
- Mitigation:
  - Comprehensive documentation (runbooks, architecture diagrams)
  - AI-assisted development (GitHub Copilot for faster implementation)
  - ЦРБ IT department trained on basic operations (user management, restarts)
Risk 6: User Adoption Resistance
- Likelihood: Medium (change management challenge)
- Impact: High (low adoption = project failure)
- Mitigation:
  - UAT with real ЦРБ employees (Feb 2026)
  - Training sessions before go-live
  - Gradual rollout: pilot with 1 department first
  - Feedback loops: weekly surveys first month
Risk 7: Telegram Bot API Rate Limits
- Likelihood: Medium (зависит от notification volume)
- Impact: Medium (notification delays или failures)
- Mitigation:
  - Research Telegram Bot API rate limits (30 messages/sec/bot documented limit)
  - Implement notification queue с rate limiting (max 20 messages/sec sustained)
  - Email fallback for failed notifications (automatic retry after 30s)
  - Monitor notification throughput Week 1 (Grafana alerts at >15 msg/sec)
  - Circuit breaker pattern: disable Telegram notifications if >50% failure rate
Risk 8: JSONB Performance at Scale
- Likelihood: Low (PostgreSQL 17.6 GIN indexes well-optimized)
- Impact: Medium (audit log queries slow if misconfigured)
- Mitigation:
  - GIN index on audit_logs.payload (строка 1097) - created in initial migration
  - Performance testing Week 8 с 100K+ audit records (JMeter load test)
  - Query optimization guide в architecture document
  - Monitoring: alert if audit queries >500ms p95
  - Archival strategy: move >1 year old audit logs to cold storage (S3 or filesystem)
---
## Success Metrics & KPIs
Tracking Frequency: Weekly for first month, then monthly
Dashboard Metrics:
1. **Adoption Rate:** % of employees who created ≥1 ticket
2. **Response Time SLA:** % of tickets meeting <4h first response
3. **Resolution Time:** Average time-to-resolution by priority
4. **User Satisfaction:** CSAT score from post-ticket surveys
5. **System Uptime:** % uptime (measured by monitoring)
6. **Support Agent Efficiency:** Tickets resolved per agent per day
Quarterly Review:
- Stakeholder presentation with metrics dashboard
- Feedback collection for roadmap adjustments
---
## Appendix A: User Stories (Complete List)
Employee Role (Ticket Creator):
- US-1: Create ticket with title, description, category, priority
- US-2: View my tickets list with filters and search
- US-3: View ticket details and history
- US-4: Receive notifications about my ticket updates
- US-5: Update my profile (contact info)
- US-6: Change my password
Support Role (Ticket Handler):
- US-7: View assigned and unassigned tickets in my categories
- US-8: Assign ticket to myself (self-assignment)
- US-9: Add comments to ticket
- US-10: Update ticket status (Open → In Progress → Resolved)
- US-11: Close ticket after resolution
- US-12: View ticket history and audit trail
- US-13: View basic analytics (my performance, workload)
Admin Role (System Manager):
- US-14: Approve/reject new user registrations
- US-15: Assign roles to users (promote Employee → Support)
- US-16: View all tickets across all categories
- US-17: Reassign tickets between agents
- US-18: View comprehensive analytics and export reports
- US-19: View complete audit logs for compliance
- US-20: Manage system configuration (notification settings, categories)
---
## Appendix B: Glossary

- **Ticket:** IT support request created by employee
- **Category:** Type of issue (Hardware, Software, Network, Access, Other)
- **Priority:** Urgency level (P1=Critical, P2=High, P3=Low)
- **Status:** Ticket lifecycle state (Open, In Progress, Resolved, Closed)
- **Assignment:** Linking ticket to specific support agent
- **Audit Log:** Immutable record of all system actions
- **SLA:** Service Level Agreement (target response/resolution time)
- **RBAC:** Role-Based Access Control
- **ABAC:** Attribute-Based Access Control (ownership checks)
- **NFR:** Non-Functional Requirement
- **RTO:** Recovery Time Objective (max downtime)
- **RPO:** Recovery Point Objective (max data loss)

---

## Appendix C: API Pagination & Search Specification

### Pagination

**Default and Limits:**

- Default page size: 20 items/page
- Maximum page size: 100 items/page
- Page numbering: 1-based (first page is `?page=1`)

**Query Parameters:**

- `page` (integer, default=1): Page number
- `size` (integer, default=20, max=100): Items per page
- `sort` (string): Sort field and direction, e.g., `created_at,desc`

**Response Format:**

```json
{
  "content": [...],
  "page": 1,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8,
  "first": true,
  "last": false
}
```

**Error Handling:**

- **400 Bad Request** if `page < 1` or `size > 100`
- **400 Bad Request** if `sort` field is invalid

### Search Fields

**Tickets Endpoint (`/api/v1/tickets`):**

- **title** (weighted 2x): Full-text search with higher relevance
- **description** (weighted 1x): Full-text search with standard relevance
- **Performance:** <500ms p95 for searches on 50K tickets

**Users Endpoint (`/api/v1/users`):**

- **full_name**: Partial match search
- **email**: Exact and partial match
- **department**: Exact match filter

**Search Query Parameter:**

- `q` (string): Search query across configured fields
- Example: `/api/v1/tickets?q=printer&page=1&size=20`

**Error Responses:**

```json
{
  "timestamp": "2025-11-05T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid pagination parameters",
  "errors": [
    {
      "field": "size",
      "message": "Page size must not exceed 100"
    }
  ]
}
```

---

## Appendix D: Attachment Storage Plan

### MVP/Phase 1 (Filesystem Storage)

**Storage Location:**

- Path: `/var/lib/tech-support/attachments/`
- Filesystem: ext4
- Allocated capacity: 1TB

**File Organization:**

- Directory structure: `/{year}/{month}/{ticket_id}/{file_uuid}.{ext}`
- Example: `/var/lib/tech-support/attachments/2026/03/550e8400-e29b.../photo.jpg`

**Constraints:**

- Max file size: 10MB per file
- Max files per ticket: 5 files
- Allowed types: Images (jpg, png), PDFs, text files, logs

### Phase 2 Trigger (MinIO Migration)

**Migration Conditions:**

- Storage usage >50GB OR
- Multi-server deployment required

**Target Architecture:**

- MinIO S3-compatible object storage
- Multi-tier storage: Hot (SSD) + Cold (HDD archive tier)

### Archive Strategy

**Archival Rules:**

- Files older than 2 years → Move to cold storage
- Archive tier: MinIO archive tier or S3 Glacier-compatible
- Access time: Cold storage retrieval <4 hours

**Monitoring:**

- Weekly storage checks (automated cron job)
- Alert threshold: 80% capacity (800GB)
- Dashboard metric: Current storage usage % in Grafana

---
## Sign-off

**Prepared by:** Business Analyst Mary  
**Reviewed by:** Nag (Technical Lead)  
**Date:** 2025-11-06  
**Version:** 1.2 (Consolidated)  
**Status:** ✅ Approved for Architecture Phase  
**Next Workflow:** Architecture Document (create-architecture)

### Consolidation Notes

This version consolidates three separate PRD documents:

1. **PRD.md** - Original comprehensive requirements
2. **PRD-refinements-2025-11-05.md** - Performance metrics (p50/p95/p99), API pagination, attachment storage, incident response
3. **PRD-JSONB-update-2025-11-05.md** - JSONB requirements, schema validation, performance guidelines

**Key Improvements in v1.2:**

- ✅ Performance SLAs aligned with NFR template (p50/p95/p99 percentiles)
- ✅ Frontend API contract unambiguous (pagination, search fields, error responses)
- ✅ Infrastructure sizing enabled (attachment storage plan with migration path)
- ✅ Dependency versions verified (no re-checking during build.gradle creation)
- ✅ Incident response compliance-ready (notification chain, timelines, escalation)
- ✅ Log sanitization rules concrete (regex patterns, rotation schedule, verification)
- ✅ JSONB usage guidelines comprehensive (must use / must not use with rationale)

### Stakeholder Approval

- [ ] Technical Lead: Nag
- [ ] Project Sponsor: ЦРБ Марьина Горка IT Department

---

_This PRD provides complete functional and non-functional requirements for the Tech-Support helpdesk system. All requirements are grounded in the brainstorming session and technical research, ensuring alignment with the modular monolith architecture and AI-assisted development approach._

_Version 1.2 consolidates all refinements and updates into a single source of truth, ready for architecture phase._
