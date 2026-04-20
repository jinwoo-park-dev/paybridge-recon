# ADR-001 — Choose a companion reconciliation service with server to server PayBridge reads

## Context

PayBridge already owns approval, reversal, webhook handling, audit rows, and outbox rows.

The reconciliation workflow must satisfy several constraints at the same time:

- preserve a clean boundary between money movement and post transaction comparison
- avoid direct database coupling to PayBridge
- keep operator case review on one same origin application surface
- reuse PayBridge read models instead of reimplementing payment logic

---
## Options considered

### Option 1 — Add reconciliation directly inside PayBridge
Pros:
- one deployable unit
- no extra service-to-service HTTP call

Cons:
- expands the core payment service into settlement import and case-triage responsibilities
- makes the repository harder to review as the payment surface grows
- increases the risk of coupling reconciliation concerns to payment write paths

### Option 2 — Companion reconciliation service that reads PayBridge over HTTP
Pros:
- keeps the payment system focused on execution and lifecycle recording
- keeps reconciliation logic isolated and reviewable
- avoids direct database access across service boundaries
- lets the browser stay on one same origin operator surface

Cons:
- adds an upstream dependency on PayBridge read APIs
- requires explicit handling for partial upstream failures

### Option 3 — Direct database reads from PayBridge tables
Pros:
- could avoid some HTTP round trips

Cons:
- creates tight schema coupling across repositories
- makes local setup and future evolution harder
- weakens the value of PayBridge read-model APIs

---
## Decision

Choose **Option 2 — a companion reconciliation service that reads PayBridge over HTTP**.

---
## Consequences

### Positive
- Payment execution and reconciliation stay separate.
- Recon can depend on PayBridge read models instead of storage internals.
- The browser never needs PayBridge operator credentials.
- The case detail API can aggregate local and upstream data into one operator-friendly response.

### Negative / trade-offs
- Recon must tolerate PayBridge unavailability.
- Upstream API shape changes need to remain deliberate.
- Some data is duplicated intentionally as persisted run snapshots.

---
## Implementation notes

- `integration.paybridge.PayBridgeOpsClient` owns upstream HTTP calls.
- `run.ReconRunService` persists one snapshot row per exported payment.
- `casework.ReconCaseQueryService` loads optional PayBridge detail, audit logs, and outbox events only for case review.
- The browser only talks to `paybridge-recon` endpoints.
