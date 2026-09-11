(ns riceops.facts-test
  (:require [clojure.test :refer [deftest is are testing]]
            [riceops.facts :as facts]))

(deftest supply-category-lookup
  (testing "Lookup valid supply category"
    (let [c (facts/supply-category-by-id "seed")]
      (is (= "seed" (:id c)))
      (is (= "種苗" (:name c)))))

  (testing "Lookup invalid supply category"
    (is (nil? (facts/supply-category-by-id "unknown")))))

(deftest supply-category-cost-thresholds
  (testing "Category-specific cost thresholds"
    (are [id expected] (= expected (:cost-threshold (facts/supply-category-by-id id)))
      "seed"        500
      "fertilizer"  500
      "equipment"   1000)))

(deftest default-cost-threshold-value
  (testing "Default fallback threshold matches the conservative baseline"
    (is (= 500 facts/default-cost-threshold))))

(deftest rice-variety-lookup
  (testing "Lookup valid rice variety"
    (are [id expected-name] (= expected-name (:name (facts/rice-variety-by-id id)))
      "japonica"  "ジャポニカ米"
      "indica"    "インディカ米"
      "glutinous" "もち米"
      "aromatic"  "香り米（ジャスミン・バスマティ等）"
      "upland"    "陸稲"))

  (testing "Lookup invalid rice variety"
    (is (nil? (facts/rice-variety-by-id "unknown"))))

  (testing "Wheat is out of scope (ISIC 0111, not this actor)"
    (is (nil? (facts/rice-variety-by-id "wheat")))))

(deftest water-management-operations-reference
  (testing "Flooding/drainage operation types are present as reference data"
    (is (contains? facts/water-management-operations "flooding"))
    (is (contains? facts/water-management-operations "drainage"))
    (is (contains? facts/water-management-operations "midseason-drainage"))
    (is (contains? facts/water-management-operations "harvest-drain"))))
