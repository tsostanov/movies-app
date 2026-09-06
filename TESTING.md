# Testing Guide

## Test Pyramid

The project currently covers several layers:

- Unit tests for business services:
  - movie creation/update/delete mapping and WebSocket change events
  - movie uniqueness validation
  - CSV escaping and export formatting
  - YAML import preview and import transaction outcomes
- Web/API tests with `MockMvc`:
  - request validation errors and field-level error details
  - `404` error mapping
  - movie list paging, sort allow-list, and CSV export pagination
- Integration smoke test:
  - Spring context with PostgreSQL Testcontainers when Docker is available

## Run Locally

```powershell
.\mvnw.cmd test
```

The Spring context test is marked with `@Testcontainers(disabledWithoutDocker = true)`, so it is skipped on machines without Docker and runs automatically when Docker is available.

## Useful QA Scenarios Covered

- Positive and negative service paths
- Boundary values for paging and export size
- API validation response shape
- Structured `400` responses for malformed JSON and invalid query parameters
- Analytics search guardrails for blank and overly long substrings
- Import rollback on failed movie creation
- Import operation status transitions
- CSV escaping for commas, quotes, and line breaks
- WebSocket payload emitted after movie changes
