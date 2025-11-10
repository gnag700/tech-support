# Tech-Support System

> **Для разработчиков**: См. подробную информацию по настройке в `docs/spring-modulith-2.0-setup-guide.md`

## Обзор проекта

Tech-Support - это модульная монолитная система технической поддержки, построенная на Spring Boot 4.0 и Spring Modulith 2.0. Система обеспечивает комплексное управление заявками, пользователями, аудитом, аналитикой, уведомлениями и базой знаний.

## Архитектура

### Модульная структура

Проект использует **Spring Modulith** для организации кода в виде модульного монолита с чёткими границами между модулями:

- **usermanagement** - управление пользователями и ролями
- **ticketing** - управление заявками и их жизненным циклом
- **audit** - аудит действий пользователей
- **analytics** - аналитика и отчёты
- **notification** - система уведомлений
- **knowledgebase** - база знаний и документация

Каждый модуль разделён на:
- `api/` - публичные интерфейсы для межмодульного взаимодействия
- `impl/` - внутренняя реализация (недоступна другим модулям)

### Технологический стек

- **Java 21 LTS**
- **Spring Boot 4.0.0-RC1**
- **Spring Modulith 2.0.0-RC1**
- **PostgreSQL 17**
- **Gradle 8.11.1**
- **Flyway 11.1.0** (миграции БД)
- **Testcontainers 1.20.4** (интеграционные тесты)

## Требования

- JDK 21 или выше
- Gradle 8.11.1 или выше (минимум 8.2 для Spring Modulith 2.0)
- PostgreSQL 17
- Docker (для Testcontainers)

## Установка и запуск

### 1. Клонирование репозитория

```bash
git clone <repository-url>
cd tech-support
```

### 2. Настройка базы данных

Создайте PostgreSQL базу данных:

```sql
CREATE DATABASE techsupport;
CREATE USER techsupport_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE techsupport TO techsupport_user;
```

### 3. Конфигурация

Создайте файл `src/main/resources/application-local.yml` с локальными настройками:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/techsupport
    username: techsupport_user
    password: your_password
```

### 4. Сборка проекта

```bash
./gradlew clean build
```

### 5. Запуск приложения

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

Приложение будет доступно по адресу: http://localhost:8080

## Разработка

### Профили окружений

- **local** - локальная разработка
- **staging** - тестовое окружение
- **prod** - production окружение

Активация профиля:

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

или через переменную окружения:

```bash
export SPRING_PROFILES_ACTIVE=local
./gradlew bootRun
```

### Запуск тестов

```bash
# Все тесты
./gradlew test

# Только unit-тесты
./gradlew test --tests '*Test'

# Только интеграционные тесты
./gradlew test --tests '*IT'

# С отчётом покрытия
./gradlew test jacocoTestReport
```

### Code Quality

Проект использует Spotless для форматирования кода:

```bash
# Проверка форматирования
./gradlew spotlessCheck

# Автоматическое форматирование
./gradlew spotlessApply
```

### Modulith Boundary Tests

Проект включает архитектурные тесты для проверки границ модулей:

```bash
./gradlew test --tests 'ModulithBoundaryTest'
```

## Структура проекта

```
tech-support/
├── build.gradle                 # Gradle конфигурация
├── settings.gradle              # Настройки проекта
├── gradle.properties            # Профили и настройки сборки
├── gradle/
│   └── libs.versions.toml       # Каталог версий зависимостей
├── src/
│   ├── main/
│   │   ├── java/com/techsupport/
│   │   │   ├── TechSupportApplication.java
│   │   │   ├── usermanagement/
│   │   │   │   ├── package-info.java
│   │   │   │   ├── api/         # Публичный API
│   │   │   │   └── impl/        # Внутренняя реализация
│   │   │   ├── ticketing/
│   │   │   ├── audit/
│   │   │   ├── analytics/
│   │   │   ├── notification/
│   │   │   └── knowledgebase/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-staging.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/    # Flyway миграции
│   └── test/
│       └── java/com/techsupport/
│           └── ModulithBoundaryTest.java
├── docs/                        # Документация
└── scripts/                     # Утилиты
```

## Документация

Подробная документация доступна в директории `docs/`:

- [Архитектурные решения](docs/architecture-2025-11-06.md)
- [Product Requirements](docs/PRD.md)
- [Technical Specifications](docs/tech-spec-epic-1.md)
- [DevOps Strategy](docs/devops-strategy-2025-11-06.md)

## Вклад в проект

См. [CONTRIBUTING.md](CONTRIBUTING.md) для деталей по стандартам разработки и процессу внесения изменений.

## Лицензия

[Укажите лицензию проекта]
