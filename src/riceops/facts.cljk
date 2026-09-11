(ns riceops.facts
  "Reference facts for rice-paddy operations coordination: supply category
  cost policy, rice-variety classification, and water-management operation
  types. This namespace contains pure lookup functions for domain reference
  data -- the Governor and Advisor consult these instead of inventing
  thresholds. Mirrors `cerealops.facts` (cloud-itonami-isic-0111) in shape,
  adapted to paddy-rice specifics (flooding/drainage water-level management
  rather than generic dryland field ops).")

(def supply-categories
  "Procurement categories this actor may propose orders for, and the
  default cost threshold above which an order proposal must escalate for
  human sign-off (farmer/ops-manager)."
  {"seed"
   {:id "seed" :name "種苗" :cost-threshold 500}

   "fertilizer"
   {:id "fertilizer" :name "肥料" :cost-threshold 500}

   "equipment"
   {:id "equipment" :name "設備（灌漑ポンプ等含む）" :cost-threshold 1000}})

(defn supply-category-by-id [id]
  (get supply-categories id))

(def default-cost-threshold
  "Fallback escalation threshold used when a supply-order proposal doesn't
  cite a known category (never invent a lower bar than this)."
  500)

(def rice-varieties
  "Rice varieties this actor's field records may cover (ISIC 0112: growing
  of rice -- other cereals are ISIC 0111, out of scope)."
  {"japonica"  {:id "japonica" :name "ジャポニカ米"}
   "indica"    {:id "indica" :name "インディカ米"}
   "glutinous" {:id "glutinous" :name "もち米"}
   "aromatic"  {:id "aromatic" :name "香り米（ジャスミン・バスマティ等）"}
   "upland"    {:id "upland" :name "陸稲"}})

(defn rice-variety-by-id [id]
  (get rice-varieties id))

(def water-management-operations
  "Reference set of flooding/drainage water-management operation types this
  actor's schedule-field-operation proposals commonly cover. Informational
  only -- NOT a validated enum; the advisor/operator may propose other
  operation-type strings (e.g. \"planting\"/\"harvest\") and the Governor
  does not reject unlisted values here."
  #{"flooding" "drainage" "midseason-drainage" "harvest-drain"})
