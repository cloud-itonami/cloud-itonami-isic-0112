# Operator Quickstart — Rice-Paddy Operations Coordination

Shortest path from clone to a verified local dry-run for **ISIC 0112** (`cloud-itonami-isic-0112`).

## Prerequisites

- Clojure 1.12+ (`clojure --version`)
- Java 17+
- Git

No invented metrics; this is a governed OSS blueprint, not a hosted SaaS demo.

## 1. Clone

```bash
git clone https://github.com/cloud-itonami/cloud-itonami-isic-0112.git
cd cloud-itonami-isic-0112
```

## 2. Run tests

```bash
clojure -M:dev:test
```

Expect green if maturity is `implemented`. Fix failures before operating.
(`:dev:test` resolves `langgraph`/`langchain-store`/`langchain` via the
in-monorepo sibling checkouts at `../../kotoba-lang/*`; a standalone fork
should override those `:local/root` coordinates in `deps.edn` first.)

## 3. Open the product face

```bash
open docs/index.html   # or: python3 -m http.server -d docs 8080
```

Publish: enable GitHub Pages on `main` `/docs`, or any static host.

## 4. Where the Governor sits

- Blueprint governor key: `paddy-operations-governor`
- Likely source path: `riceops.governor.cljc`
- Pattern: intake → advise → govern → decide → commit | request-approval →
  commit | hold, compiled to a `langgraph-clj` `StateGraph`
  (`riceops.operation`, itonami actor / ADR-2607011000)

## 5. Claim / go-live

- Free claim funnel: https://itonami.cloud/isco-1212/
- Paid path docs: https://itonami.cloud/docs/go-live.md
- Blueprint: `blueprint.edn`

## Constraints

- Do not invent users/revenue numbers for marketing
- No force-push; keep AGPL headers
- Secrets stay out of this repo
