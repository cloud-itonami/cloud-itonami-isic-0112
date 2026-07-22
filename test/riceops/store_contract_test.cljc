(ns riceops.store-contract-test
  "MemStore ≡ DatomicStore parity for the Store protocol. Mirrors
  `cerealops.store-contract-test` (cloud-itonami-isic-0111)."
  (:require [clojure.test :refer [deftest is]]
            [riceops.store :as store]))

(defn- exercise [s]
  (store/add-field s "paddy-x" {:id "paddy-x" :name "X Paddy" :variety "japonica"})
  ;; re-registering (update) exercises the identity-upsert path on
  ;; DatomicStore (:field/id is :db.unique/identity) the same way
  ;; MemStore's plain `assoc` re-registration does.
  (store/add-field s "paddy-x" {:id "paddy-x" :name "X Paddy (renamed)" :variety "japonica"})
  (store/append-ledger! s {:t :committed :op :log-field-record :subject "paddy-x"})
  (store/append-ledger! s {:t :approval-requested :op :flag-crop-health-concern :subject "paddy-x"})
  {:field  (store/registered-field s "paddy-x")
   :absent (store/registered-field s "no-such-paddy")
   :ledger (store/ledger s)})

(deftest mem-and-datomic-parity
  (let [mem (store/mem-store)
        dat (store/datomic-store)
        m (exercise mem)
        d (exercise dat)]
    (is (= (:field m) (:field d)))
    (is (= "X Paddy (renamed)" (:name (:field m))) "re-registration upserts, not forks history")
    (is (nil? (:absent m)))
    (is (nil? (:absent d)))
    (is (= 2 (count (:ledger m))))
    (is (= 2 (count (:ledger d))))
    (is (= (:ledger m) (:ledger d)))))

(deftest datomic-store-seeded-fields
  (let [dat (store/datomic-store {:initial-fields
                                   {"paddy-y" {:id "paddy-y" :name "Y Paddy"}}})]
    (is (= {:id "paddy-y" :name "Y Paddy"} (store/registered-field dat "paddy-y")))
    (is (empty? (store/ledger dat)))))
