# Student Edition Guide

## Target

Keep existing ERP business logic stable, while making the codebase easier for students to understand, run, and defend in a graduation project.

## Refactor principles

1. Behavior first: no changes to business results unless explicitly required.
2. Small safe steps: prioritize low-risk cleanup and observability.
3. Config over hardcode: credentials and paths must be externalized.
4. Readability over cleverness: clear naming, constants, and focused methods.

## Scope of this batch

- Configuration normalization and de-sensitive defaults.
- Script robustness improvements (`.bat` and `.sh`).
- Readability refactor in startup and filter modules.
- Documentation for team onboarding and review.

## Suggested next batches

1. Feature flags for commercialization-related modules (plugin/tenant trial).
2. Permission checks centralization (avoid duplicated manager checks).
3. API and service layer contract tests for critical flows.

## Non-goals in this batch

- No schema redesign.
- No broad API contract changes.
- No removal of existing domain entities.

## Defense tips (for thesis/presentation)

- Explain "no business regression" strategy with phased delivery.
- Show one concrete code smell and its refactor (magic string -> constants).
- Show security improvement: hardcoded credentials -> environment configuration.
- Show maintainability metrics: fewer repeated literals, clearer startup scripts.
