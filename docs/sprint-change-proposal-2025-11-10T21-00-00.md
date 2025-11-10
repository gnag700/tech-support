# Sprint Change Proposal: Maven → Gradle Migration

**Дата:** 2025-11-10  
**Проект:** Tech-Support  
**Инициатор:** Nag (Product Manager)  
**Статус:** Готов к утверждению

---

## 1. Issue Summary

### Проблема
Обнаружено критическое несоответствие между архитектурным решением и технической спецификацией проекта Tech-Support:

- **Architecture Document** (architecture-2025-11-06.md) явно указывает **Gradle 8.11.1** как build tool
- **Technical Specification** (tech-spec-epic-1.md) и **Story 1-1** используют **Maven**
- **PRD** (PRD.md) указывает Maven 3.9+

### Контекст обнаружения
Проблема выявлена в Story 1-1 (статус: drafted) до начала реализации. Разработчик заметил противоречие при подготовке к implementation и инициировал workflow корректировки курса.

### Доказательства
- architecture-2025-11-06.md:39 - \	ype=gradle-project\
- architecture-2025-11-06.md:201 - "Gradle 8.11.1" в таблице версий
- architecture-2025-11-06.md:237 - "Build Tool (Backend) | Gradle | 8.11.1"
- tech-spec-epic-1.md содержит 20+ упоминаний Maven
- PRD.md:827 - "Build: Maven 3.9+"
- Story 1-1 AC-1.1.2 - "Настроены Maven-зависимости..."

---

## 2. Impact Analysis

### 2.1 Epic Impact

**Epic 1: Foundation & Infrastructure Enablement**
- ✅ Может быть завершён как запланировано
- 🔄 Требуется обновление терминологии и примеров команд
- ❌ Scope и цели НЕ меняются

**Epic 2-7:**
- ✅ НЕ затронуты - упоминания build tool только в Epic 1
- ✅ Зависят от инфраструктуры, но не от конкретного build tool

### 2.2 Story Impact

**Story 1-1** (drafted):
- 🔴 Критическое - требует полного обновления
- AC-1.1.2: Maven → Gradle terminology
- AC-1.1.4: \mvn verify\ → \gradle build\
- Tasks 1-7: множественные изменения команд и файлов

**Stories 1.2-1.5** (backlog):
- 🟡 Умеренное - потребуют обновления при drafting
- Story 1.2: CI/CD команды
- Story 1.3: Docker build с Gradle

**Stories 2.1+** (backlog):
- ✅ НЕ затронуты

### 2.3 Artifact Conflicts

| Артефакт | Конфликт | Изменения |
|----------|----------|-----------|
| **PRD.md** | ⚠️ Да | 3 упоминания Maven → Gradle |
| **architecture-2025-11-06.md** | ✅ Нет | Уже использует Gradle (источник истины) |
| **tech-spec-epic-1.md** | ⚠️ Да | ~15 упоминаний + workflows + traceability |
| **epics.md** | ⚠️ Да | Story 1.1 и 1.2 AC + technical notes |
| **Story 1-1** | ⚠️ Да | AC, tasks, dev notes, примеры команд |
| **devops-strategy-2025-11-06.md** | ✅ Нет | Уже использует Gradle везде |
| **ux-design-specification.md** | ✅ N/A | Не применимо |
| **validation-report*.md** | ✅ Нет | Не содержат упоминаний |

### 2.4 Technical Impact

**Положительное влияние:**
- ✅ Gradle обеспечивает лучшее кэширование сборок
- ✅ Быстрее incremental builds
- ✅ Современный подход для Spring Boot проектов
- ✅ Совместимость с Java 21 гарантирована

**Нейтральное:**
- ⚪ Функциональность идентична Maven
- ⚪ Все Spring Boot возможности доступны
- ⚪ Плагины (Spotless, JaCoCo, Flyway) поддерживаются обоими

**Отсутствие негативного влияния:**
- ✅ Нет технических ограничений
- ✅ Нет потери функциональности
- ✅ Нет влияния на runtime поведение

---

## 3. Recommended Approach

### 3.1 Выбранный путь: **Direct Adjustment**

**Обоснование:**
1. **Минимальные усилия:** Только документационные изменения (~2-4 часа)
2. **Низкий риск:** Story 1-1 в статусе drafted, код не написан
3. **Нет влияния на timeline:** Изменения до начала implementation
4. **Устраняет путаницу:** Единообразие документации
5. **Соответствие Architecture:** Architecture doc = источник истины

### 3.2 Отклонённые альтернативы

**Rollback (Option 2):**
- ❌ Не применимо - нет кода для отката

**MVP Review (Option 3):**
- ❌ Избыточно - Maven vs Gradle не влияет на MVP scope

### 3.3 Effort Estimate

- **Документация:** 2-4 часа
- **Количество изменений:** ~40-50 мест
- **Риск:** 🟢 Low
- **Влияние на timeline:** ⚪ Нулевое

---

## 4. Detailed Change Proposals

### 4.1 PRD.md Changes

**Change 1: Technology Stack**
\\\diff
- Build: Maven 3.9+
+ Build: Gradle 8.11.1
\\\

**Change 2: Timeline Section**
\\\diff
-   - Validate all dependencies Week 1 (pom.xml verification)
+   - Validate all dependencies Week 1 (build.gradle verification)
\\\

**Change 3: Dependency Management**
\\\diff
- - ✅ Dependency versions verified (no re-checking during pom.xml creation)
+ - ✅ Dependency versions verified (no re-checking during build.gradle creation)
\\\

### 4.2 tech-spec-epic-1.md Changes

**Change 4: Story 1.1 AC**
\\\diff
- **AC-1.1.2:** Настроены Maven-зависимости, root BOM, профили...
+ **AC-1.1.2:** Настроены Gradle-зависимости, dependency management, профили...

- **AC-1.1.4:** Результаты сборки успешно проходят локальный mvn verify.
+ **AC-1.1.4:** Результаты сборки успешно проходят локальный gradle build.
\\\

**Change 5: Story 1.2 AC**
\\\diff
- **AC-1.2.2:** Выполняются цели mvn verify, spotless:check...
+ **AC-1.2.2:** Выполняются цели gradle build, spotlessCheck...

- **AC-1.2.4:** Кэшируются зависимости Maven и Docker-слои...
+ **AC-1.2.4:** Кэшируются зависимости Gradle и Docker-слои...
\\\

**Change 6: Workflows Section**
\\\diff
Story 1.1: Repository Initialization Flow
- 1. Create Git repo with .gitignore (Maven, IDE, OS-specific)
+ 1. Create Git repo with .gitignore (Gradle, IDE, OS-specific)
- 2. Initialize Maven multi-module project structure
+ 2. Initialize Gradle project structure with Spring Boot plugin
- 3. Configure Spring Boot parent POM with dependency management
+ 3. Configure build.gradle with dependency management
- 7. Run mvn clean verify to validate structure
+ 7. Run gradle build to validate structure

Story 1.2: CI/CD Pipeline Flow
- 2. Cache Maven dependencies (.m2/repository)
+ 2. Cache Gradle dependencies (.gradle directory)
- 3. Run mvn clean verify (compile + test + integration tests)
+ 3. Run gradle build (compile + test + integration tests)
- 4. Run mvn spotless:check (code formatting)
+ 4. Run gradle spotlessCheck (code formatting)
\\\

**Change 7: Performance + Open Questions**
\\\diff
**Build Performance:**
- - Maven clean verify completes in ≤3 минут...
+ - Gradle build completes in ≤3 минут...

**QUESTION-1:** Использовать Maven или Gradle?
- - *Current Decision:* Maven (указано в Architecture doc)...
+ - *RESOLVED:* Gradle 8.11.1 (согласно Architecture doc). Обеспечивает лучшее кэширование...
\\\

**Change 8: Traceability Matrix + Infrastructure**
\\\diff
- | AC-1.1.1 | ... | Maven modules, @ApplicationModule | ...
+ | AC-1.1.1 | ... | Gradle modules, @ApplicationModule | ...
- | AC-1.1.2 | ... | pom.xml | Maven dependency:tree...
+ | AC-1.1.2 | ... | build.gradle | Gradle dependencies task...

**Local Development:**
- - Maven 3.9.x
+ - Gradle 8.11.1

- - Настроен для Maven ecosystem
+ - Настроен для Gradle ecosystem
\\\

### 4.3 epics.md Changes

**Change 9: Story 1.1 and 1.2 AC**
\\\diff
Story 1.1:
- 2. Настроены Maven-зависимости, root BOM, профили...
+ 2. Настроены Gradle-зависимости, dependency management, профили...
- 4. Результаты сборки успешно проходят локальный mvn verify.
+ 4. Результаты сборки успешно проходят локальный gradle build.

- - Создать pom.xml с централизованным управлением...
+ - Создать build.gradle с централизованным управлением...

Story 1.2:
- 2. Выполняются цели mvn verify, spotless:check...
+ 2. Выполняются цели gradle build, spotlessCheck...
- 4. Кэшируются зависимости Maven и Docker-слои...
+ 4. Кэшируются зависимости Gradle и Docker-слои...
\\\

### 4.4 Story 1-1 Changes

**Change 10: Complete Story Update**
\\\diff
## Acceptance Criteria
- **AC-1.1.2:** Настроены Maven-зависимости, root BOM...
+ **AC-1.1.2:** Настроены Gradle-зависимости, dependency management...
- **AC-1.1.4:** Результаты сборки успешно проходят локальный mvn verify.
+ **AC-1.1.4:** Результаты сборки успешно проходят локальный gradle build.

## Tasks / Subtasks
Task 1:
-   - [ ] Создать Git репозиторий с .gitignore (Maven, IDE, OS-specific)
+   - [ ] Создать Git репозиторий с .gitignore (Gradle, IDE, OS-specific)

- - [ ] **Task 2: Настроить Maven parent POM и dependency management**
+ - [ ] **Task 2: Настроить Gradle build configuration и dependency management**
-   - [ ] Создать parent pom.xml с Spring Boot 3.5.7 parent
+   - [ ] Создать build.gradle с Spring Boot plugin 3.5.7
-   - [ ] Настроить dependencyManagement с Spring Modulith BOM...
+   - [ ] Настроить dependency management с Spring Modulith BOM...
-   - [ ] Настроить плагины: maven-compiler-plugin (Java 21), spotless, jacoco, flyway
+   - [ ] Настроить плагины: java (toolchain Java 21), spotless, jacoco, flyway

- - [ ] **Task 4: Настроить core dependencies в pom.xml**
+ - [ ] **Task 4: Настроить core dependencies в build.gradle**

Task 7:
-   - [ ] Запустить mvn clean compile - проверить успешную компиляцию
+   - [ ] Запустить gradle compileJava - проверить успешную компиляцию
-   - [ ] Запустить mvn test - проверить прохождение unit tests
+   - [ ] Запустить gradle test - проверить прохождение unit tests
-   - [ ] Запустить mvn verify - проверить full build cycle
+   - [ ] Запустить gradle build - проверить full build cycle
-   - [ ] Проверить активацию профилей: mvn help:active-profiles -Plocal
+   - [ ] Проверить активацию профилей через -Plocal

## Dev Notes
- **Maven Structure:**
+ **Gradle Structure:**
- - Single-module Maven project (NOT multi-module Maven)
+ - Single-project Gradle build (NOT multi-project Gradle)
- - Shared parent POM for dependency versions
+ - Centralized dependency management in build.gradle
\\\

---

## 5. Implementation Handoff

### 5.1 Change Scope Classification

**Scope:** ✅ **Minor**

**Обоснование:**
- Документационные изменения только
- Не требует code rollback (код не написан)
- Не влияет на архитектурные решения
- Не требует backlog reorganization

### 5.2 Handoff Recipients

**Primary:** 🎯 **Product Manager (сам инициатор)**
- Выполнение всех document updates
- Финальная проверка согласованности

**Secondary:** 📢 **Scrum Master**
- Уведомление о готовности Story 1-1 к dev handoff
- Обновление sprint-status.yaml (если требуется)

**Tertiary:** 👨‍💻 **Development Team**
- Информирование об изменениях в Story 1-1
- Использование Gradle при implementation

### 5.3 Deliverables

1. ✅ Обновлённые файлы (4 документа):
   - docs/PRD.md
   - docs/tech-spec-epic-1.md
   - docs/epics.md
   - docs/stories/1-1-initializirovat-modulnyj-monolit-i-strukturu-repozitoriya.md

2. ✅ Sprint Change Proposal (этот документ)

3. ✅ Implementation Plan (ниже)

### 5.4 Success Criteria

- [ ] Все 4 документа обновлены с Maven → Gradle
- [ ] Терминология единообразна во всех артефактах
- [ ] Architecture doc остаётся source of truth
- [ ] Story 1-1 готова к передаче Dev команде
- [ ] Нет упоминаний Maven в Epic 1 документации
- [ ] Change Log в Story 1-1 обновлён

---

## 6. Implementation Plan

### 6.1 Execution Steps

**Step 1:** Обновить PRD.md
- [ ] Изменение 1: Technology Stack (строка 827)
- [ ] Изменение 2: Timeline Section (строка 939)
- [ ] Изменение 3: Dependency Management (строка 1190)

**Step 2:** Обновить tech-spec-epic-1.md
- [ ] Изменение 4: Story 1.1 AC (строки 270-274)
- [ ] Изменение 5: Story 1.2 AC (строки 280-284)
- [ ] Изменение 6: Workflows Section (строки 71-82)
- [ ] Изменение 7: Performance + Questions (строки 112, 382)
- [ ] Изменение 8: Traceability + Infrastructure (строки 320-322, 250, 242)

**Step 3:** Обновить epics.md
- [ ] Изменение 9: Story 1.1 and 1.2 (строки 702, 710, 720, 722)

**Step 4:** Обновить Story 1-1
- [ ] Изменение 10: Complete update (AC, Tasks, Dev Notes)

**Step 5:** Финальная проверка
- [ ] Grep search всех docs/ на наличие оставшихся "maven|Maven"
- [ ] Проверка consistency между documents
- [ ] Update Change Log в Story 1-1

### 6.2 Timeline

- **Effort:** 2-4 часа работы PM
- **Execution:** Может быть выполнено в один session
- **Review:** Self-review + optional SM check
- **Completion:** В течение 1 рабочего дня

### 6.3 Risk Mitigation

**Риск:** Пропуск упоминаний Maven
- **Mitigation:** Финальный grep search по всему docs/

**Риск:** Несогласованность терминологии
- **Mitigation:** Checklist всех 4 документов cross-reference

**Риск:** Confusion для Dev при handoff
- **Mitigation:** Явное уведомление + brief на Story 1-1

---

## 7. Summary

### 7.1 Change Overview

- **Issue:** Maven vs Gradle несоответствие
- **Scope:** 4 документа, ~40-50 изменений
- **Effort:** 2-4 часа
- **Risk:** 🟢 Low
- **Timeline Impact:** ⚪ None

### 7.2 Artifacts Modified

1. ✅ PRD.md (3 изменения)
2. ✅ tech-spec-epic-1.md (15+ изменений)
3. ✅ epics.md (4 изменения)
4. ✅ Story 1-1 (20+ изменений)

### 7.3 Next Steps

1. ✅ **PM:** Выполнить все document updates
2. 📢 **SM:** Уведомить о готовности Story 1-1
3. 👨‍💻 **Dev:** Приступить к implementation с Gradle

---

**✅ Sprint Change Proposal Complete**

**Prepared by:** Product Manager Agent (John)  
**Approved by:** Nag (User)  
**Date:** 2025-11-10  
**Status:** Ready for Implementation

