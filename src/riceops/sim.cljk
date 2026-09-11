(ns riceops.sim
  "Demo driver -- `clojure -M:dev:run`. Walks a registered paddy field
  through a clean phase-3 auto-commit, an always-escalate crop-health
  concern (human approves), a high-cost supply order (human rejects),
  and a hard-hold (unregistered field), then prints the resulting
  audit ledger. Mirrors `cerealops.sim` (cloud-itonami-isic-0111)."
  (:require [langgraph.graph :as g]
            [riceops.operation :as operation]
            [riceops.store :as store]))

(def farmer {:actor-id "rice-ops-01" :role :farm-operator :phase :phase-3})

(defn- exec-op [actor tid request context]
  (g/run* actor {:request request :context context} {:thread-id tid}))

(defn- approve! [actor tid]
  (g/run* actor {:approval {:status :approved :by "rice-ops-01"}}
          {:thread-id tid :resume? true}))

(defn- reject! [actor tid]
  (g/run* actor {:approval {:status :rejected :by "rice-ops-01"}}
          {:thread-id tid :resume? true}))

(defn demo
  "Run the compiled StateGraph through a commit path, an
  escalate->approve->commit path, an escalate->reject->hold path, and
  a hard-hold path; print each result and the final audit ledger."
  []
  (let [st (store/mem-store
            {:initial-fields
             {"paddy-001"
              {:id "paddy-001"
               :name "Test Farm North Paddy"
               :variety "japonica"}}})
        actor (operation/build st)]

    (println "=== Rice-Paddy Operations Coordinator Demo ===")

    (println "\n== log-field-record paddy-001 (phase-3, governor-clean -> commit) ==")
    (println (exec-op actor "t1"
                      {:op :log-field-record :field-id "paddy-001"
                       :acreage 80 :water-level 5 :variety "japonica"
                       :record-type "planting"}
                      farmer))

    (println "\n== flag-crop-health-concern paddy-001 (ALWAYS escalates -- farmer approves) ==")
    (let [r (exec-op actor "t2"
                     {:op :flag-crop-health-concern :field-id "paddy-001"
                      :concern "いもち病の疑い"}
                     farmer)]
      (println r)
      (println "-- farmer/agronomist approves --")
      (println (approve! actor "t2")))

    (println "\n== order-supplies paddy-001 over cost threshold (escalates -- farmer rejects) ==")
    (let [r (exec-op actor "t3"
                     {:op :order-supplies :field-id "paddy-001"
                      :category "equipment" :cost 1200}
                     farmer)]
      (println r)
      (println "-- farmer rejects --")
      (println (reject! actor "t3")))

    (println "\n== log-field-record paddy-999 (unregistered -> HARD hold, no interrupt) ==")
    (println (exec-op actor "t4"
                      {:op :log-field-record :field-id "paddy-999"
                       :acreage 50 :water-level 3 :variety "indica"}
                      farmer))

    (println "\n== audit ledger ==")
    (doseq [f (store/ledger st)] (println f))

    {:ledger (store/ledger st)}))

(defn -main
  "clojure -M:run entrypoint."
  [& _args]
  (demo))

(comment
  ;; In a real REPL:
  (demo)
  )
