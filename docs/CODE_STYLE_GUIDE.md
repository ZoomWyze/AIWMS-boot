# Backend Code Style Guide

## Java conventions

- Use meaningful names (`requestUrl`, `publicUrls`, `containsIllegalPathTraversal`).
- Avoid magic strings; extract constants at class level.
- Keep methods short and single-purpose.
- Prefer early return to reduce deep nesting.
- Keep comments concise; explain intent, not syntax.

## Configuration conventions

- No hardcoded secrets in repository.
- Prefer `${ENV_VAR:default}` style in properties.
- Keep comments readable and encoding-safe.
- Separate runtime-specific values from source defaults.

## Script conventions

- Scripts should run with and without `JAVA_HOME`.
- Check directory existence using semantic checks (`-d`).
- Avoid obsolete JVM options when possible.

## API and filter conventions

- Centralize public endpoint lists to constants/collections.
- Keep security checks explicit and testable.
- Keep response behavior unchanged during cleanup refactors.

## Review checklist

- [ ] No business logic behavior changes.
- [ ] No secret leakage in config or logs.
- [ ] No duplicated literals for key endpoints.
- [ ] Startup and deployment scripts still work.
- [ ] Build passes (`mvn clean package -DskipTests`).
