# ADR-002 — Prefer manual import and manual run before scheduler automation

## Context

A realistic reconciliation system can grow toward scheduled runs, recurring settlement ingestion, and retry handling. That wider scope would add substantial infrastructure and failure mode complexity.

For the current repository, the primary goal is to make the comparison workflow explicit and reviewable:

- import one settlement batch
- select a comparison window
- capture PayBridge snapshots
- generate discrepancy cases
- review them in an operator workbench

---
## Options considered

### Option 1 — Scheduler first design
Pros:
- closer to a production automation story on paper

Cons:
- adds scheduling, storage ingestion, retry, locking, and operational alerting concerns immediately
- makes the repository larger without improving the core comparison model proportionally

### Option 2 — Manual import and manual run first
Pros:
- keeps the important domain behavior visible
- reduces infrastructure surface area
- makes local verification straightforward

Cons:
- operators must trigger runs explicitly
- no recurring execution story in the first version

---
## Decision

Choose **Option 2 — manual import and manual run first**.

---
## Consequences

### Positive
- The repository stays small and easier to review.
- The import → snapshot → match → case pipeline stays obvious.
- Local smoke tests remain deterministic.

### Negative / trade-offs
- The project does not yet model scheduled daily runs.
- Import sources are limited to manual CSV upload.
- Retry and run locking concerns remain future work.

---
## Implementation notes

- `/imports` owns batch upload and history.
- `/runs/new` owns explicit run creation.
- `ReconRunService` captures all PayBridge snapshots for the selected request window.
- Future automation can be added later without rewriting the existing case domain.
