# cloud-itonami-isic-0112

Open Occupation Blueprint for **ISIC Rev. 4 0112**: Growing of rice.

This repository implements a forkable OSS **rice-paddy operations
coordinator**: a field-management and record-keeping robot manages planting/
yield/water-level logging, paddy-field-operation scheduling (planting,
flooding/drainage water management, harvest), and supply procurement under a
governor-gated actor, so a rice farm keeps its own operational records and
maintains full transparency over decisions.

**Maturity: `:implemented`.** `src/riceops/` implements the
`RiceOpsAdvisor` (`riceops.advisor`) and the independent
`PaddyOperationsGovernor` (`riceops.governor`), composed by
`riceops.operation` following the itonami actor pattern (ADR-2607011000):
`intake -> advise -> govern -> decide -> commit | request-approval ->
commit | hold`, compiled to a real `langgraph-clj` `StateGraph`
(`langgraph.graph/state-graph` + `compile-graph`, mirroring
`cerealops.operation`, cloud-itonami-isic-0111) with
`interrupt-before #{:request-approval}` and checkpoint-based
human-in-the-loop resume for escalated operations. Every commit/hold/
approval-rejected decision fact is appended to `riceops.store`'s
append-only audit ledger (`ledger`/`append-ledger!`), implemented on
both `MemStore` and a `DatomicStore` (backed by `langchain.db` via
`kotoba-lang/langchain-store`) that pass the same store-contract test
(`test/riceops/store_contract_test.cljc`). The demo runner
(`clojure -M:dev:run`) drives the compiled graph end-to-end through a
commit path, an escalate→approve→commit path, an escalate→reject→hold
path, and a hard-hold path, printing the resulting audit ledger. The
GitHub Pages operator console (`docs/samples/operator-console.html`,
`clojure -M:dev:render-html`) is generated the same way, including a
genuine checkpoint resume for one resolved escalation.

## What this does NOT do

This actor coordinates **back-office logistics only**. It explicitly does **NOT**:

- **Direct field/irrigation-equipment operation** (flooding/drainage valves,
  pumps) — remains the farmer's exclusive authority
- **Pesticide-application decisions** — remains the agronomist/farmer authority
- **Agronomic decision authority** (what/when/how much to plant, spray, flood,
  drain, or harvest) — remains human authority; this actor only coordinates
  the logistics around those decisions
- **Direct execution of any kind** — any proposal for direct field/irrigation-
  equipment control or finalizing a pesticide-application decision is a hard
  block

## HARD invariants (always hold, never overridable)

1. **field-not-registered** — the request's `field-id` must resolve to a
   registered paddy field in the Store before any proposal can proceed
2. **no-execution** — every proposal's `:effect` must be `:propose` (the
   governor never directly operates field/irrigation equipment, never
   finalizes a pesticide-application decision)
3. **equipment-or-pesticide-decision-blocked** — `:operate-field-equipment`,
   `:operate-irrigation-equipment`, and `:finalize-pesticide-application`
   proposals are unconditionally, permanently blocked
4. **op-not-allowed** — any op outside the closed allowlist below is rejected
5. **field-record-invalid** — `:log-field-record` with a non-positive acreage
   is rejected
6. **water-level-invalid** — `:log-field-record` with a negative water-level
   is rejected (zero, a drained/dry paddy, is valid)

## Always-escalate operations (human sign-off, regardless of confidence)

- `:flag-crop-health-concern` — any pest/disease/blast-fungus/drought-stress
  concern → automatic escalation
- `:order-supplies` over its category cost threshold (default 500 currency
  units; see `riceops.facts/supply-categories`)
- Any proposal with confidence below the Governor's floor (0.7)

## Operational requests (closed allowlist, all `:effect :propose`)

```text
:log-field-record
  — record planting/yield/water-level/soil-test data
  — requires a registered paddy field; non-positive acreage or negative
    water-level is rejected

:schedule-field-operation
  — propose a planting/flooding-drainage/harvest scheduling operation
  — does NOT make agronomic decisions

:flag-crop-health-concern
  — surface a pest, disease, blast-fungus, or drought-stress concern
  — ALWAYS escalates for human review

:order-supplies
  — procurement for seed, fertilizer, equipment (including irrigation pumps)
```

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs the
physical domain work**. Here a field-management robot handles:

- Paddy field record logging and entry (acreage, yield, water-level)
- Field-operation scheduling and reminders (planting/flooding-drainage/harvest)
- Supply inventory and ordering
- Audit ledger maintenance

The **PaddyOperationsGovernor** is the independent safety layer that gates all
proposals before a robot action is executed. The governor never dispatches
hardware directly; `:high`/`:safety-critical` actions (such as escalated
crop-health concerns or high-cost supply orders) require human sign-off.

## Core Contract

```text
operational request (log, schedule, concern, order)
        |
        v
RiceOpsAdvisor -> PaddyOperationsGovernor -> phase gate -> commit, or escalate for human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated operation can dispatch a robot action the governor refuses, suppress an
operating record, or hide a crop-health concern without governor approval and audit
evidence.

## Module structure

Mirrors `cloud-itonami-isic-0111` (`cerealops.*`) module-for-module, with a
paddy-specific water-level check added:

- `riceops.facts` — reference data: supply-category cost thresholds, rice
  varieties, water-management operation types
- `riceops.registry` — pure independent verification functions
  (cost/acreage/water-level/confidence)
- `riceops.store` — `Store` protocol: field registration lookup + append-only
  audit ledger, implemented by `MemStore` (in-memory, default) and
  `DatomicStore` (`langchain.db`-backed, via `kotoba-lang/langchain-store`)
- `riceops.advisor` — `Advisor` protocol + `MockAdvisor` (the sealed LLM/
  decision node; a real-LLM `Advisor` implementation is the documented next
  seam, same as every sibling cloud-itonami actor's advisor)
- `riceops.governor` — `PaddyOperationsGovernor`: hard invariants + escalation
  gates
- `riceops.phase` — 0→3 rollout phase gate
- `riceops.operation` — compiles the `langgraph-clj` `StateGraph`: advise →
  govern → decide → commit | request-approval → commit | hold, with
  `interrupt-before` + checkpoint-based resume for escalated operations
- `riceops.sim` — demo runner (`clojure -M:dev:run`)
- `riceops.render-html` — build-time renderer for
  `docs/samples/operator-console.html`, driving the same compiled StateGraph
  (`clojure -M:dev:render-html`)

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISIC Rev. 4 `0112`). Required capabilities:

- :robotics
- :identity
- :forms
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## Testing

```bash
clojure -M:dev:test   # run the test suite (langgraph/langchain-store resolved via local sibling checkouts)
clojure -M:lint       # clj-kondo, 0 errors / 0 warnings
clojure -M:dev:run    # demo runner -- drives the compiled StateGraph end-to-end
```

`:dev` pins the transitive `langchain` dependency to the in-monorepo local
checkout (`../../kotoba-lang/langchain`) for offline workspace development;
a standalone fork should override `deps.edn`'s `:local/root` coordinates
with git coordinates (see `deps.edn`'s own comment).

## License

AGPL-3.0-or-later.
