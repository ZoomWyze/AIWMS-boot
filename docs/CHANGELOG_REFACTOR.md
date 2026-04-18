# Refactor Changelog

## 2026-04-12 (Batch 1: Low-risk cleanup)

### Code

- Updated `src/main/java/com/jsh/erp/ErpApplication.java`
  - Startup output now derives URL from `server.port` and `server.servlet.context-path`.
  - Removed hardcoded demo account prompt from console output.

- Updated `src/main/java/com/jsh/erp/filter/LogCostFilter.java`
  - Introduced constants/sets for public and special-case endpoints.
  - Extracted path traversal check method to improve readability.
  - Preserved original login interception behavior.

### Config

- Updated `src/main/resources/application.properties`
  - Replaced garbled comments with readable comments.
  - Added environment-variable override support for DB/Redis/upload paths.
  - Removed hardcoded DB password default.

### Scripts

- Updated `src/main/bin/start.bat`
  - Supports optional `JAVA_HOME` and clearer command structure.
  - Keeps existing JVM memory defaults.

- Updated `src/main/bin/run-manage.sh`
  - Replaced outdated PermGen options with Metaspace options.
  - Fixed directory existence check and conditional `JAVA_HOME` permission handling.

### Docs

- Added `README.md`
- Added `docs/STUDENT_EDITION_GUIDE.md`
- Added `docs/CODE_STYLE_GUIDE.md`

## Rollback points

- All changes are file-local and can be reverted per-file.
- No schema or API contract changes introduced in this batch.
- If startup scripts are not used in your environment, backend runtime behavior remains unaffected by script changes.
