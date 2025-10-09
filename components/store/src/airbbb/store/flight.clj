(ns airbbb.store.flight
  (:require
   [datomic.api :as d]))

(def rules
  '[[(available ?flight ?wanted-amount)
     [?flight :flight/amount ?flight-amount]
     [(q '[:find [?t ...] :in $ ?f
           :where  [?t :ticket/flight ?f]]
         $ ?flight) ?ticket]
     [(count ?ticket) ?ticket-count]
     [(- ?flight-amount ?ticket-count) ?flight-rest]
     [(< ?wanted-amount ?flight-rest)]]])

(defn available? [db eid amount]
  (some?
   (d/q '[:find ?flight .
          :in $ % ?flight ?wanted-amount
          :where (available ?flight ?wanted-amount)] db rules eid amount)))

(defn by-id [db id]
  (d/entity db [:flight/id id]))

(defn by-from-to [db from to]
  (d/q '[:find ?flight .
         :in $ ?flight-from ?flight-to
         :where [?flight :flight/from ?flight-from]
         [?flight :flight/to ?flight-to]] db from to))

