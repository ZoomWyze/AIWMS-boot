# AIWMS-boot (Student Edition Refactor)

This repository keeps the original ERP business flow while improving readability, maintainability, and deployment safety for graduation-project usage.

## What changed in this iteration

- Removed hardcoded sensitive configuration defaults (`application.properties` now supports env overrides).
- Cleaned and standardized configuration comments (English, readable, no garbled text).
- Refactored startup output in `ErpApplication` to avoid hardcoded context path and demo credentials.
- Refactored login filter constants in `LogCostFilter` to reduce magic strings and improve maintainability.
- Improved startup scripts in `src/main/bin` for clearer behavior and better JDK compatibility.
- Added student-focused documentation under `docs/`.

## Quick start

### 1) Prepare dependencies

- JDK 8+ (project build should follow `pom.xml` settings)
- MySQL 5.7+/8.x
- Redis

### 2) Import database

Use `docs/aiwms_erp.sql` to initialize schema/data.

### 3) Configure environment variables (recommended)

- `MYSQL_URL`
- `MYSQL_USERNAME`
- `MYSQL_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_PASSWORD`
- `FILE_UPLOAD_PATH`
- `TOMCAT_BASEDIR`

If unset, defaults in `src/main/resources/application.properties` are used.

### 4) Run backend

```bash
mvn clean package -DskipTests
java -jar target/AIWMS.jar
```

## Documentation

- `docs/STUDENT_EDITION_GUIDE.md`: goals, scope, and simplification strategy.
- `docs/CODE_STYLE_GUIDE.md`: backend coding conventions for this project.
- `docs/CHANGELOG_REFACTOR.md`: refactor log and rollback notes.

## Notes

This iteration is a low-risk cleanup pass. It intentionally avoids changing core domain behavior (orders, inventory, finance flows).
