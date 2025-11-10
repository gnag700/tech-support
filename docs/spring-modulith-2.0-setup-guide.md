# Spring Modulith 2.0 RC1 Setup Guide

> **Важно**: Этот документ содержит актуальную информацию по настройке Spring Modulith 2.0 RC1 для проекта Tech-Support.
> Обновлено: 2025-11-10

## Обзор

Spring Modulith 2.0 находится в стадии Release Candidate. Последняя доступная версия - **2.0.0-RC1** (опубликована в октябре 2025).

### Ключевые требования

- **Spring Modulith версия**: 2.0.0-RC1
- **Spring Boot версия**: 4.0.0-RC1 или новее
- **Минимальная версия Gradle**: 8.2
- **Java**: 21+
- **Maven репозиторий**: Spring Milestone (`https://repo.spring.io/milestone`)

## Текущее состояние проекта

✅ **Gradle версия**: 8.11.1 (удовлетворяет требованию ≥ 8.2)  
⚠️ **Spring Modulith в docs**: Документы указывают 2.0.0-RC2 (не существует)  
⚠️ **Spring Boot в docs**: Документы указывают 4.0.0-RC2 (возможно не финальная версия)

## Настройка Gradle

### 1. Version Catalog (`gradle/libs.versions.toml`)

**АКТУАЛЬНАЯ КОНФИГУРАЦИЯ** для использования RC версий:

```toml
[versions]
# Используем RC версии Spring Boot 4.0 и Spring Modulith 2.0
springBoot = "4.0.0-RC1"
springModulith = "2.0.0-RC1"
java = "21"

# Database
postgresql = "42.7.4"
flyway = "11.1.0"

# Testing
testcontainers = "1.20.4"

# Build plugins
spotless = "6.25.0"
jacoco = "0.8.12"

[libraries]
# Spring Boot starters
spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web" }
spring-boot-starter-data-jpa = { module = "org.springframework.boot:spring-boot-starter-data-jpa" }
spring-boot-starter-security = { module = "org.springframework.boot:spring-boot-starter-security" }
spring-boot-starter-validation = { module = "org.springframework.boot:spring-boot-starter-validation" }
spring-boot-starter-actuator = { module = "org.springframework.boot:spring-boot-starter-actuator" }
spring-boot-starter-test = { module = "org.springframework.boot:spring-boot-starter-test" }

# Spring Modulith - используем BOM для управления версиями
spring-modulith-bom = { module = "org.springframework.modulith:spring-modulith-bom", version.ref = "springModulith" }
spring-modulith-starter-core = { module = "org.springframework.modulith:spring-modulith-starter-core" }
spring-modulith-starter-jpa = { module = "org.springframework.modulith:spring-modulith-starter-jpa" }
spring-modulith-events-jdbc = { module = "org.springframework.modulith:spring-modulith-events-jdbc" }
spring-modulith-starter-test = { module = "org.springframework.modulith:spring-modulith-starter-test" }
spring-modulith-observability = { module = "org.springframework.modulith:spring-modulith-observability" }
spring-modulith-actuator = { module = "org.springframework.modulith:spring-modulith-actuator" }

# Database
postgresql = { module = "org.postgresql:postgresql", version.ref = "postgresql" }
flyway-core = { module = "org.flywaydb:flyway-core", version.ref = "flyway" }
flyway-database-postgresql = { module = "org.flywaydb:flyway-database-postgresql", version.ref = "flyway" }

# Testing
testcontainers-bom = { module = "org.testcontainers:testcontainers-bom", version.ref = "testcontainers" }
testcontainers-postgresql = { module = "org.testcontainers:postgresql" }
testcontainers-junit-jupiter = { module = "org.testcontainers:junit-jupiter" }

[plugins]
spring-boot = { id = "org.springframework.boot", version.ref = "springBoot" }
spring-dependency-management = { id = "io.spring.dependency-management", version = "1.1.7" }
spotless = { id = "com.diffplug.spotless", version.ref = "spotless" }

[bundles]
# Основные модули Spring Modulith
spring-modulith = [
    "spring-modulith-starter-core",
    "spring-modulith-starter-jpa", 
    "spring-modulith-events-jdbc"
]
flyway = ["flyway-core", "flyway-database-postgresql"]
testcontainers = ["testcontainers-postgresql", "testcontainers-junit-jupiter"]
```

### 2. Build.gradle Configuration

**КРИТИЧЕСКИ ВАЖНО**: Добавить Spring Milestone репозиторий!

```gradle
plugins {
    id 'java'
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.spotless)
    id 'jacoco'
}

group = 'com.techsupport'
version = '0.0.1-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    // ОБЯЗАТЕЛЬНО для RC версий!
    maven { url 'https://repo.spring.io/milestone' }
}

dependencyManagement {
    imports {
        // Используем BOM для управления версиями Spring Modulith
        mavenBom libs.spring.modulith.bom.get().toString()
    }
}

dependencies {
    // Spring Boot starters
    implementation libs.spring.boot.starter.web
    implementation libs.spring.boot.starter.data.jpa
    implementation libs.spring.boot.starter.validation
    implementation libs.spring.boot.starter.actuator

    // Spring Modulith - основной bundle
    implementation libs.bundles.spring.modulith
    
    // Spring Modulith - дополнительные модули (runtime)
    runtimeOnly libs.spring.modulith.observability
    runtimeOnly libs.spring.modulith.actuator

    // Database
    runtimeOnly libs.postgresql
    implementation libs.bundles.flyway

    // Testing
    testImplementation libs.spring.boot.starter.test
    testImplementation libs.spring.modulith.starter.test
    testImplementation platform(libs.testcontainers.bom)
    testImplementation libs.bundles.testcontainers
}

tasks.named('test') {
    useJUnitPlatform()
}
```

## Структура модулей

### Package-based Modularity

Spring Modulith 2.0 использует **package-based** модульность (НЕ multi-project Gradle).

```text
src/main/java/com/techsupport/
├── TechSupportApplication.java          # @SpringBootApplication
├── usermanagement/
│   ├── package-info.java                # @ApplicationModule
│   ├── api/                             # Публичный API
│   └── impl/                            # Внутренняя реализация
├── ticketing/
│   ├── package-info.java
│   ├── api/
│   └── impl/
├── audit/
├── analytics/
├── notification/
└── knowledgebase/
```

### Package-info.java Template

```java
@org.springframework.modulith.ApplicationModule(
    displayName = "User Management"
)
package com.techsupport.usermanagement;

import org.springframework.modulith.ApplicationModule;
```

### Main Application Class

```java
package com.techsupport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TechSupportApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TechSupportApplication.class, args);
    }
}
```

## Testing

### Modulith Boundary Test

```java
package com.techsupport;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Spring Modulith Boundary Test
 * Verifies module structure and generates documentation
 */
class ModulithBoundaryTest {

    private final ApplicationModules modules = 
        ApplicationModules.of(TechSupportApplication.class);

    @Test
    void verifyModularStructure() {
        // Проверяет архитектурные правила:
        // - Нет циклических зависимостей
        // - Доступ только к API модулей
        // - Соблюдение allowedDependencies
        modules.verify();
    }

    @Test
    void generateModuleDocumentation() {
        // Генерирует PlantUML диаграммы
        new Documenter(modules)
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml();
    }
}
```

### Module-specific Integration Test

```java
package com.techsupport.usermanagement;

import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.ApplicationModuleTest.BootstrapMode;
import org.junit.jupiter.api.Test;

@ApplicationModuleTest(mode = BootstrapMode.STANDALONE)
class UserManagementModuleTest {
    
    @Test
    void testModule() {
        // Загружается только модуль usermanagement
    }
}
```

## Configuration

### application.yml

```yaml
spring:
  application:
    name: tech-support
    
  modulith:
    # Включить event publication registry
    events:
      publication-registry:
        enabled: true
      # Retention policy (30 дней)
      retention-policy: P30D
      # Переотправка незавершенных событий при старте
      republish-outstanding-events-on-restart: true

logging:
  level:
    org.springframework.modulith: DEBUG
```

## Event-Driven Architecture

### Publishing Events

```java
package com.techsupport.order;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ApplicationEventPublisher events;

    @Transactional
    public void completeOrder(Order order) {
        order.markComplete();
        // Публикация события для других модулей
        events.publishEvent(new OrderCompleted(order.getId()));
    }
}
```

### Consuming Events

```java
package com.techsupport.inventory;

import com.techsupport.order.OrderCompleted;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
class InventoryService {

    // Гарантированная доставка события
    @ApplicationModuleListener
    void onOrderCompleted(OrderCompleted event) {
        // Обновить инвентарь
        // Даже если приложение упадет, событие будет переотправлено
    }
}
```

## Troubleshooting

### Проблема: Spring Modulith artifacts не найдены

**Решение**: Убедитесь, что добавлен Spring Milestone репозиторий:

```gradle
repositories {
    mavenCentral()
    maven { url 'https://repo.spring.io/milestone' }
}
```

### Проблема: Несовместимость версий

**Важно**: Spring Modulith 2.0 требует Spring Boot 4.0:

```toml
springBoot = "4.0.0-RC1"
springModulith = "2.0.0-RC1"
```

### Проблема: Gradle version incompatible

Spring Modulith 2.0 требует минимум Gradle 8.2. Проверить версию:

```bash
./gradlew --version
```

Обновить wrapper (если нужно):

```bash
./gradlew wrapper --gradle-version 8.14
```

## Миграция со стабильной версии

Если вы начали со Spring Modulith 1.x:

### Ключевые изменения 1.x → 2.0

1. **Требование Spring Boot 4.0** (вместо 3.x)
2. **Новая аннотация**: `@Modulithic` вместо `@Modulith`
3. **Package detection**: Улучшенная автоматическая детекция модулей
4. **Event API**: Новые абстракции для event externalization

### Рекомендация для Production

⚠️ **Для production приложений**: Дождитесь GA (General Availability) релизов Spring Boot 4.0 и Spring Modulith 2.0.

✅ **Для новых проектов**: RC версии подходят для разработки и тестирования.

## Полезные ссылки

- [Spring Modulith Reference Documentation](https://docs.spring.io/spring-modulith/reference/)
- [Spring Modulith GitHub](https://github.com/spring-projects/spring-modulith)
- [Spring Blog - Modulith 2.0 RC1 Announcement](https://spring.io/blog/2025/10/27/spring-modulith-2-0-rc1-1-4-4-and-1-3-10-released)
- [Spring Milestone Repository](https://repo.spring.io/milestone/)

## Обновление документации проекта

После применения этих изменений необходимо обновить:

1. ✅ `gradle/libs.versions.toml` - версии зависимостей
2. ✅ `build.gradle` - добавить milestone репозиторий и BOM
3. ⚠️ `docs/tech-spec-epic-1.md` - исправить версии с RC2 на RC1
4. ⚠️ `docs/tech-spec-story-1-1.md` - исправить версии
5. ⚠️ `docs/stories/1-1-*.md` - обновить примеры кода
6. ⚠️ `README.md` - обновить секцию Tech Stack

## Проверка конфигурации

После обновления зависимостей выполните:

```bash
# Очистить кэш Gradle
./gradlew clean

# Проверить разрешение зависимостей
./gradlew dependencies --configuration runtimeClasspath

# Запустить тесты
./gradlew test

# Проверить Modulith структуру
./gradlew test --tests 'ModulithBoundaryTest'
```

---

**Последнее обновление**: 2025-11-10  
**Версия документа**: 1.0  
**Автор**: Product Manager Agent
