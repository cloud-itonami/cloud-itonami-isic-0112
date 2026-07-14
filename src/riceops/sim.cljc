(ns riceops.sim
  "Simple simulation/demo runner for the Rice-Paddy Operations Coordinator
  actor. Used to validate that the actor flow compiles and basic proposal
  flow works. Mirrors `cerealops.sim` (cloud-itonami-isic-0111)."
  (:require [riceops.operation :as operation]
            [riceops.store :as store]))

(defn demo
  "Run a simple demo scenario: register a paddy field, propose a
  field-record log (with water-level), and check the disposition flow."
  []
  (let [;; Create store with a registered paddy field
        st (store/mem-store
            {:initial-fields
             {"paddy-001"
              {:id "paddy-001"
               :name "Test Farm North Paddy"
               :variety "japonica"}}})

        ;; Build actor
        actor (operation/build st)

        ;; Create a request to log a field record
        request {:op :log-field-record
                 :field-id "paddy-001"
                 :acreage 80
                 :water-level 5
                 :variety "japonica"
                 :record-type "planting"}

        ;; Context with phase 0 (simulation)
        context {:actor-id "rice-ops-01"
                 :role :farm-operator
                 :phase :phase-0}]

    (println "=== Rice-Paddy Operations Coordinator Demo ===")
    (println "Demo field: paddy-001")
    (println "Request: log-field-record")
    (println "Phase: phase-0 (simulation)")
    (println "Expected: escalate (phase-0 forces human review of all commits)")
    (println)
    (let [result (actor request context)]
      (println "Result disposition:" (:disposition result))
      result)))

(defn -main
  "clojure -M:run entrypoint."
  [& _args]
  (demo))

(comment
  ;; In a real REPL:
  (demo)
)
