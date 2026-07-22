# Operator Guide: Rice-Paddy Operations Coordinator

## Overview

The Rice-Paddy Operations Coordinator is a field-management robot that:

1. **Logs operational data** — planting acreage, yield, water-level, soil-test notes
2. **Schedules coordination** — planting/flooding-drainage/harvest windows, supply orders
3. **Escalates concerns** — any crop pest/disease/blast-fungus/drought-stress issue
4. **Maintains transparency** — audit ledger traces all decisions

The robot is **not** the decision-maker. The farmer/agronomist make all
decisions about agronomic practice, water management, pesticide application,
and economic choices. The robot **proposes** actions and escalates when
human input is needed.

## Operating the Actor

### Prerequisites

1. **Paddy Field Registration** — your field must be registered in the
   system before any operation can proceed
2. **Authorized User** — operator must be authenticated and authorized
3. **Clear Request Type** — specify what you're doing:
   - `:log-field-record` — record planting/yield/water-level/soil-test data
   - `:schedule-field-operation` — arrange a planting/flooding-drainage/
     harvest window
   - `:flag-crop-health-concern` — report a concern
   - `:order-supplies` — procurement request

### Workflow

1. **Submit Request**
   ```clojure
   {:field-id "paddy-001"
    :op :log-field-record
    :acreage 80
    :water-level 5
    :variety "japonica"
    :record-type "planting"}
   ```

2. **Actor Processes** (`(riceops.operation/build store)`, a compiled
   `langgraph-clj` `StateGraph` run via `(langgraph.graph/run* actor
   {:request request :context context} {:thread-id tid})`)
   - `:advise` — `RiceOpsAdvisor` proposes an action (`riceops.advisor`)
   - `:govern` — `PaddyOperationsGovernor` checks hard invariants and escalation gates (`riceops.governor`)
   - `:decide` — rollout-phase constraints applied on top of the Governor's verdict (`riceops.phase`)

3. **Outcomes** (`:disposition` on the return value)
   - **`:commit`** — operation logged, robot proceeds (`:record` is present)
   - **`:escalate`** — operation held pending human decision (audit fact `:t :approval-requested`)
   - **`:hold`** — operation blocked, hard violation (audit fact `:t :governor-hold`, cites `:violations`)

### Escalation Scenarios

**Automatic escalation (always human sign-off):**
- `:flag-crop-health-concern` — any pest/disease/blast-fungus/drought-stress issue
- Supply orders over cost threshold (default 500 currency units)
- Low confidence operations (< 0.7)

**Hard blocks (no override):**
- `:operate-field-equipment` — direct machinery operation is the farmer's authority
- `:operate-irrigation-equipment` — direct flooding/drainage valve/pump control is the farmer's authority
- `:finalize-pesticide-application` — pesticide-application decisions are agronomist/farmer authority
- Missing/unregistered paddy field — must register first
- Non-positive acreage or negative water-level in a logged field record

### Resuming Escalated Operations

`riceops.operation/build` compiles a real `langgraph-clj` `StateGraph` with
`interrupt-before #{:request-approval}`: an `:escalate` disposition pauses
the run at the `:request-approval` node (`(langgraph.graph/run* actor
{:request request :context context} {:thread-id tid})` returns
`:status :interrupted`) instead of returning a final decision. A human
operator (farmer/agronomist) resumes the SAME `thread-id` once a decision
is made:

```clojure
(langgraph.graph/run* actor {:approval {:status :approved :by "op-1"}}
                      {:thread-id tid :resume? true})
```

resulting in `:disposition :commit` (approved) or `:disposition :hold`
(rejected, fact `:t :approval-rejected`). See `riceops.sim`'s
`approve!`/`reject!` helpers and `riceops.render-html`'s
`resolve-approval!` for worked examples.

## Audit & Transparency

Every operation run's final state carries an `:audit` vector containing an
advisor-proposal trace and one or more disposition facts (`:committed`,
`:governor-hold`, `:approval-requested`, `:approval-granted`,
`:approval-rejected`). The `:commit`/`:hold` graph nodes append every
committed/held/approval-rejected decision fact to `riceops.store`'s
append-only ledger (`store/ledger` / `store/append-ledger!`) automatically
— no separate integration step required.

- Every proposal produces a trace, regardless of outcome
- Every hold cites the specific Governor rule(s) violated (`:violations`)
- Every escalation cites its `:reason` (always-escalate op / high cost / low confidence)

## Integration

The actor provides a standard protocol (`riceops.store/Store`) for backend
integration:

- **Field lookup** — `(store/registered-field store field-id)`
- **Field registration** — `(store/add-field store field-id field-data)`
- **Audit ledger read** — `(store/ledger store)`
- **Audit ledger append** — `(store/append-ledger! store fact)`

Implementations include in-memory `MemStore` (default, `riceops.store`) and
a `DatomicStore` backed by `langchain.db` via `kotoba-lang/langchain-store`
(the same seam point all cloud-itonami actors use); both pass the same
store-contract test (`test/riceops/store_contract_test.cljc`).

## Safety Guarantees

- **No unsupervised decisions** — no agronomic, water-management, or
  pesticide-application decision is made by the robot
- **No suppressed concerns** — crop-health concerns cannot be hidden or delayed
- **No unlogged operations** — every action is recorded in the audit ledger
- **No direct execution** — the governor gates every robot action

The robot is safe because:
1. It never decides — it proposes
2. It always escalates when needed
3. It never hides information
4. Every action is auditable
