# Contributing

**Maturity: `:implemented`** — `src/riceops/` implements the reference
RiceOpsAdvisor / PaddyOperationsGovernor actor, composed by
`riceops.operation/build` into a real `langgraph-clj` `StateGraph`
(`intake -> advise -> govern -> decide -> commit | request-approval ->
commit | hold`, `interrupt-before #{:request-approval}` + checkpoint-based
resume), with every commit/hold/approval-rejected decision landing in
`riceops.store`'s append-only ledger (`MemStore` and a `DatomicStore`
backed by `kotoba-lang/langchain-store`). Contributions that extend
coverage are welcome: a real LLM `Advisor` implementation (the sealed
`riceops.advisor/Advisor` injection point), a real Datomic/kotoba-server
backend for `DatomicStore` (currently `langchain.db`'s in-process
implementation), additional Governor rules, and rice-variety/jurisdiction
reference-data expansion in `riceops.facts`. Open an issue or PR. License:
AGPL-3.0-or-later.
