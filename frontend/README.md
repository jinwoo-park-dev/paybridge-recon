# PayBridge Recon frontend workspace

This directory contains the React and TypeScript source for the case workbench at `/workbench/cases`.

The frontend scope is intentionally narrow:

- queue filters
- case queue selection
- case detail rendering
- case status updates
- case note creation

Imports, run creation, and system inspection remain server-rendered in the Spring Boot application.

---
## Recommended local workflow

1. Start `paybridge-recon` on `http://localhost:8081`.
2. Sign in once through `http://localhost:8081/operator/login` so the browser receives an operator session.
3. Use the Spring-rendered workbench route at `http://localhost:8081/workbench/cases` for the simplest same-origin path.
4. If you want JSX/TS iteration, use Node 24, run `npm ci` and `npm run dev`, then open `http://localhost:5174`.

---
## Commands

```bash
npm run dev
npm run test
npm run build
npm run build:runtime
```

`build:runtime` compiles the TypeScript workbench source into `src/main/resources/static/workbench/` so the Spring Boot app can serve the checked-in workbench runtime.
