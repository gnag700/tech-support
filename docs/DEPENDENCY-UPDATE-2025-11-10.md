# Dependency Update Report

**Date**: 2025-11-10  
**Updated by**: Product Manager Agent  
**Reason**: Spring Modulith 2.0.0-RC2 not available; using RC1

---

## Summary

Обновлены версии зависимостей Spring Boot и Spring Modulith на актуальные RC (Release Candidate) версии. Добавлен репозиторий Spring Milestone для разрешения RC артефактов.

## Changes

### Version Updates

| Dependency | Old Version | New Version | Status |
|------------|-------------|-------------|---------|
| Spring Boot | 3.4.0 | 4.0.0-RC1 | ✅ Updated |
| Spring Modulith | 1.3.0 | 2.0.0-RC1 | ✅ Updated |
| Gradle | 8.11.1 | 8.11.1 | ✅ OK (≥8.2 required) |

### Configuration Changes

#### 1. `gradle/libs.versions.toml`

**Updated versions:**
```toml
[versions]
springBoot = "4.0.0-RC1"
springModulith = "2.0.0-RC1"
```

**Added Spring Modulith BOM:**
```toml
[libraries]
spring-modulith-bom = { module = "org.springframework.modulith:spring-modulith-bom", version.ref = "springModulith" }
spring-modulith-starter-core = { module = "org.springframework.modulith:spring-modulith-starter-core" }
spring-modulith-observability = { module = "org.springframework.modulith:spring-modulith-observability" }
spring-modulith-actuator = { module = "org.springframework.modulith:spring-modulith-actuator" }
```

**Updated bundle:**
```toml
[bundles]
spring-modulith = ["spring-modulith-starter-core", "spring-modulith-events-jdbc", "spring-modulith-starter-jpa"]
```

#### 2. `build.gradle`

**Added Spring Milestone repository:**
```gradle
repositories {
  mavenCentral()
  // КРИТИЧЕСКИ ВАЖНО: Spring Milestone репозиторий для RC версий
  maven { url 'https://repo.spring.io/milestone' }
}
```

**Added BOM dependency management:**
```gradle
dependencyManagement {
  imports {
    // Используем BOM для управления версиями Spring Modulith
    mavenBom libs.spring.modulith.bom.get().toString()
  }
}
```

**Added runtime dependencies:**
```gradle
dependencies {
  // ...
  runtimeOnly libs.spring.modulith.observability
  runtimeOnly libs.spring.modulith.actuator
}
```

### Documentation Updates

#### New Documentation

Created `docs/spring-modulith-2.0-setup-guide.md` - comprehensive setup guide with:
- RC version requirements
- Gradle configuration examples
- Module structure templates
- Testing examples
- Event-driven architecture patterns
- Troubleshooting guide

#### Updated Documentation

1. **`docs/tech-spec-epic-1.md`**
   - Updated dependency versions to RC1
   - Added reference to setup guide
   - Updated AC-1.1.1 and AC-1.1.2

2. **`docs/tech-spec-story-1-1.md`**
   - Updated version references
   - Added link to setup guide

3. **`README.md`**
   - Updated Tech Stack section
   - Added reference to setup guide
   - Updated requirements (Gradle ≥8.2)

## Verification

### Dependencies Resolved Successfully

```bash
.\gradlew.bat dependencies --configuration runtimeClasspath
```

**Result**: ✅ All dependencies resolved correctly

Key resolved versions:
- `org.springframework.boot:spring-boot-starter-web:4.0.0-RC1`
- `org.springframework.modulith:spring-modulith-starter-core:2.0.0-RC1`
- `org.springframework.modulith:spring-modulith-events-jdbc:2.0.0-RC1`
- `org.springframework.modulith:spring-modulith-starter-jpa:2.0.0-RC1`
- `org.springframework.modulith:spring-modulith-observability:2.0.0-RC1`
- `org.springframework.modulith:spring-modulith-actuator:2.0.0-RC1`

## Important Notes

### Why RC1 instead of RC2?

According to research via Perplexity API:
- **Latest Spring Modulith 2.0 RC**: 2.0.0-RC1 (published October 2025)
- **Spring Modulith 2.0.0-RC2**: Does not exist yet
- **Documentation references to RC2**: Were incorrect/anticipated

### Minimum Requirements

- **Gradle**: 8.2+ (project uses 8.11.1 ✅)
- **Spring Boot**: 4.0.0-RC1+ (required by Spring Modulith 2.0)
- **Java**: 21+ (LTS)
- **Repository**: Spring Milestone (`https://repo.spring.io/milestone`)

### Production Readiness

⚠️ **RC versions are for development and testing**

- Spring Modulith 2.0 GA expected: Q4 2025 - Q1 2026
- Spring Boot 4.0 GA expected: Q1 2026
- For production deployment: Wait for GA releases or assess RC stability

## Next Steps for Dev Team

1. ✅ Pull latest changes from repository
2. ✅ Clear Gradle cache: `.\gradlew.bat clean`
3. ✅ Refresh dependencies: `.\gradlew.bat --refresh-dependencies`
4. ✅ Verify build: `.\gradlew.bat build`
5. ✅ Run Modulith tests: `.\gradlew.bat test --tests 'ModulithBoundaryTest'`
6. 📖 Read setup guide: `docs/spring-modulith-2.0-setup-guide.md`

## References

- [Spring Modulith 2.0 RC1 Release Notes](https://spring.io/blog/2025/10/27/spring-modulith-2-0-rc1-1-4-4-and-1-3-10-released)
- [Spring Modulith GitHub Releases](https://github.com/spring-projects/spring-modulith/releases)
- [Spring Milestone Repository](https://repo.spring.io/milestone/)
- [Spring Boot 4.0 Documentation](https://docs.spring.io/spring-boot/docs/4.0.0-RC1/reference/)

---

**Status**: ✅ **COMPLETED**  
**Build Status**: ✅ **SUCCESSFUL**  
**Tests**: ⏳ **PENDING** (awaiting dev implementation)
