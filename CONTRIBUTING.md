# Руководство по внесению вклада в Tech-Support

## Добро пожаловать!

Спасибо за ваш интерес к проекту Tech-Support! Это руководство поможет вам понять процесс разработки и стандарты кода.

## Содержание

1. [Процесс разработки](#процесс-разработки)
2. [Стандарты кода](#стандарты-кода)
3. [Соглашения по коммитам](#соглашения-по-коммитам)
4. [Тестирование](#тестирование)
5. [Code Review](#code-review)

## Процесс разработки

### Рабочий процесс

1. **Fork репозитория** (для внешних контрибьюторов)
2. **Создайте feature branch** от `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Внесите изменения** следуя стандартам кода
4. **Commit** с описательным сообщением
5. **Push** в ваш fork/branch
6. **Создайте Pull Request** в `main`

### Именование веток

- `feature/` - новая функциональность
- `bugfix/` - исправление бага
- `hotfix/` - срочное исправление для production
- `refactor/` - рефакторинг кода
- `docs/` - изменения в документации

Примеры:
- `feature/user-authentication`
- `bugfix/ticket-status-update`
- `docs/api-endpoints`

## Стандарты кода

### Java Code Style

Проект использует **Google Java Style Guide** с минимальными модификациями.

#### Основные правила:

1. **Отступы**: 2 пробела (не табы)
2. **Максимальная длина строки**: 120 символов
3. **Импорты**: группировка по static/non-static, сортировка по алфавиту
4. **Naming conventions**:
   - Classes: `PascalCase`
   - Methods/variables: `camelCase`
   - Constants: `UPPER_SNAKE_CASE`
   - Packages: `lowercase`

#### Форматирование

Используйте Spotless для автоматического форматирования:

```bash
# Проверка
./gradlew spotlessCheck

# Применение
./gradlew spotlessApply
```

### Архитектурные принципы

#### Spring Modulith

1. **Module boundaries**: используйте `@ApplicationModule` для определения модулей
2. **API separation**: 
   - Публичные интерфейсы в `api/` пакетах
   - Реализация в `impl/` пакетах (package-private)
3. **Inter-module communication**:
   - Через публичные API интерфейсы
   - Через Spring Modulith Events для асинхронной коммуникации
4. **Запрещено**:
   - Прямой доступ к `impl` пакетам других модулей
   - Циклические зависимости между модулями

#### Clean Code

1. **Методы**: максимум 20 строк кода
2. **Классы**: одна ответственность (SRP)
3. **Комментарии**: код должен быть самодокументируемым
4. **Null safety**: используйте `Optional<T>` вместо null
5. **Exceptions**: специфичные checked exceptions для бизнес-логики

### Package Structure

```
com.techsupport.{module}/
├── package-info.java        # @ApplicationModule
├── api/                     # Публичные интерфейсы
│   ├── {Feature}Service.java
│   ├── {Feature}Event.java
│   └── dto/
│       └── {Feature}Dto.java
└── impl/                    # Внутренняя реализация
    ├── {Feature}ServiceImpl.java
    ├── {Feature}Repository.java
    ├── {Feature}Entity.java
    └── {Feature}Mapper.java
```

## Соглашения по коммитам

Используйте **Conventional Commits** формат:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- `feat`: новая функциональность
- `fix`: исправление бага
- `docs`: изменения в документации
- `style`: форматирование, отсутствие логических изменений
- `refactor`: рефакторинг кода
- `test`: добавление/изменение тестов
- `chore`: изменения в build процессе, зависимостях

### Scope

Название модуля или компонента:
- `usermanagement`
- `ticketing`
- `audit`
- `analytics`
- `notification`
- `knowledgebase`
- `build`
- `ci`

### Примеры

```bash
feat(ticketing): add ticket status change event publishing

Added Spring Modulith event when ticket status changes.
Other modules can now listen to TicketStatusChangedEvent.

Closes #123
```

```bash
fix(usermanagement): correct user role validation logic

Fixed NPE when checking roles for users without assigned roles.

Fixes #456
```

## Тестирование

### Тестовая пирамида

- **70% Unit tests** - быстрые, изолированные тесты
- **25% Integration tests** - тесты с БД (Testcontainers)
- **5% E2E tests** - сквозные тесты через API

### Требования

1. **Code coverage**: минимум 80%
2. **Все тесты должны проходить** перед созданием PR
3. **Testcontainers** для интеграционных тестов
4. **MockMvc** для REST API тестов

### Naming conventions

```java
// Unit tests
public class UserServiceTest {
  @Test
  void shouldCreateUserWhenValidData() { }
  
  @Test
  void shouldThrowExceptionWhenInvalidEmail() { }
}

// Integration tests
public class UserRepositoryIT {
  @Test
  void shouldPersistUserToDatabase() { }
}
```

### Запуск тестов

```bash
# Все тесты
./gradlew test

# С coverage report
./gradlew test jacocoTestReport

# Только unit tests
./gradlew test --tests '*Test'

# Только integration tests
./gradlew test --tests '*IT'

# Modulith boundary tests
./gradlew test --tests 'ModulithBoundaryTest'
```

## Code Review

### Pull Request Checklist

Перед созданием PR убедитесь что:

- [ ] Код следует стандартам проекта
- [ ] `./gradlew spotlessApply` выполнен
- [ ] Все тесты проходят (`./gradlew test`)
- [ ] Code coverage не ниже 80%
- [ ] Добавлены/обновлены тесты для новой функциональности
- [ ] Обновлена документация (если требуется)
- [ ] Commit messages следуют Conventional Commits
- [ ] Нет merge conflicts с `main`
- [ ] Modulith boundary tests проходят

### PR Description Template

```markdown
## Описание
[Краткое описание изменений]

## Связанные issues
Closes #[номер issue]

## Тип изменений
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Как протестировано
[Опишите тестирование]

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review performed
- [ ] Comments added for complex logic
- [ ] Documentation updated
- [ ] Tests added/updated
- [ ] All tests passing
```

### Review Process

1. **Automated checks** (CI) должны пройти
2. **Минимум 1 approval** от code owner
3. **Все комментарии resolved**
4. **Merge strategy**: Squash and merge

## Вопросы?

Если у вас есть вопросы, создайте issue с тегом `question` или свяжитесь с мейнтейнерами проекта.

---

**Спасибо за ваш вклад в Tech-Support!** 🚀
