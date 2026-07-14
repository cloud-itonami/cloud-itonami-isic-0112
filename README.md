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
`advise -> govern -> phase-gate -> commit | escalate | hold`.

`riceops.operation` is a synchronous stub of this flow (see its
docstring) — production wiring into a `langgraph-clj` StateGraph with
`interrupt-before`/checkpoint-based human-in-the-loop resume for escalated
operations is deferred, mirroring `cloud-itonami-isic-0111`'s own
`cerealops.operation`.

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
- `riceops.store` — `Store` protocol + in-memory `MemStore` (paddy field
  registration lookup)
- `riceops.advisor` — `Advisor` protocol + `MockAdvisor` (the sealed LLM/
  decision node)
- `riceops.governor` — `PaddyOperationsGovernor`: hard invariants + escalation
  gates
- `riceops.phase` — 0→3 rollout phase gate
- `riceops.operation` — composes advisor → governor → phase into one
  operation run
- `riceops.sim` — demo runner (`clojure -M:run`)

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
clojure -M:test   # run the test suite
clojure -M:lint   # clj-kondo, 0 errors / 0 warnings
clojure -M:run    # demo runner
```

## License

AGPL-3.0-or-later.
