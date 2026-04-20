# ADR-003 — Use server-rendered operator pages with a thin React workbench

## Context

The repository needs a usable operator surface, but the primary value lives in backend import, matching, and case aggregation logic.

The UI must satisfy a few constraints:

- keep imports, runs, and system inspection simple
- keep the browser on same origin Recon APIs
- avoid moving comparison rules into the client
- show one focused interactive surface for queue and detail review

---
## Options considered

### Option 1 — Full SPA for the entire application
Pros:
- one UI paradigm everywhere

Cons:
- expands the frontend surface far beyond what the repository needs
- shifts attention away from backend comparison logic
- increases local setup and build complexity

### Option 2 — Server rendered pages plus a thin React workbench
Pros:
- keeps imports, runs, and system pages simple
- gives the case queue/detail workflow a focused interactive shell
- leaves all matching and aggregation logic on the backend
- keeps deployment and local run paths compact

Cons:
- mixed rendering model requires a little discipline
- the React workbench still needs a small runtime and build path

---
## Decision

Choose **Option 2 — server rendered pages plus a thin React workbench**.

---
## Consequences

### Positive
- The operator journey stays easy to follow.
- React is used where it adds the most value: queue/detail review.
- The backend remains the source of truth for filtering, detail aggregation, note writes, and status changes.

### Negative / trade-offs
- The UI stack is intentionally mixed rather than uniform.
- A small compiled workbench runtime needs to be checked in or built.
- Imports and runs remain less dynamic than a full SPA.

---
## Implementation notes

- Thymeleaf templates render `/`, `/imports`, `/runs/new`, and `/system`.
- `/workbench/cases` renders one shell div and loads the React runtime.
- `frontend/src/app/WorkbenchApp.tsx` owns queue/detail interaction only.
- Mutating requests still use same origin Recon APIs with CSRF protection.
