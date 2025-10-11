(ns airbbb.store.flight
  (:require
   [airbbb.store.core :as core]
   [datomic.api :as d]))

(def rules
  '[[(available ?flight ?wanted-amount)
     [?flight :flight/amount ?flight-amount]
     [(q '[:find [?t ...] :in $ ?f
           :where  [?t :ticket/flight ?f]]
         $ ?flight) ?ticket]
     [(count ?ticket) ?ticket-count]
     [(- ?flight-amount ?ticket-count) ?flight-rest]
     [(<= ?wanted-amount ?flight-rest)]]

    [(same-day ?time ?next-time)
     [(.toInstant ^java.util.Date ?time) ?t1]
     [(.toInstant ^java.util.Date ?next-time) ?t2]
     [(.between java.time.temporal.ChronoUnit/HOURS ?t1 ?t2) ?hours]
     [(< ?hours 24)]]

    [(can-transfer ?from ?to ?flight)
     [?flight :flight/from ?from]
     [?flight :flight/to ?to]]

    [(can-transfer ?from ?to ?flight)
     [?flight :flight/to ?next-from]
     [?next-flight :flight/from ?next-from]
     [?next-flight :flight/time ?next-time]
     [?flight :flight/time ?time]
     (same-day ?time ?next-time)

     (can-transfer ?next-from ?to ?next-flight)]])

(defn transfers-by-from-to-amount [db from to amount date]
  (->
   '{:find [?from ?to (pull ?flight [*])]
     :in [$ % [?from ...] [?to ...] ?amount]
     :where [(or-join [?flight ?from ?to]
                      [?flight :flight/to ?to]
                      (and
                       [?flight :flight/from ?from]
                       [?flight :flight/to ?to])
                      (can-transfer ?from ?to ?flight))
             (available ?flight ?amount)]}

   (assoc :args [db rules from to amount])
   (#(if date
       (-> %
           (update :in conj '[?date ...])
           (update :args conj date)
           (update :where conj
                   '[?flight :flight/date ?flight-date]
                   '(same-day ?date ?flight-date))) %))

   core/remap-query

   d/query

   (#(group-by (fn [[f s]] [f s]) %))

   (update-vals (fn [s] (map #(nth % 2) s)))))

;; (defn transfers-by-from-to-amount [db from to amount]
;;   (d/q '[:find  [(pull ?flight [*]) ...]
;;          :in $ % ?from ?to ?amount
;;          :where
;;          (or-join [?flight ?from ?to]
;;                   [?flight :flight/to ?to]
;;                   (and
;;                    [?flight :flight/from ?from]
;;                    [?flight :flight/to ?to])
;;                   (can-transfer ?from ?to ?flight))
;;          (available ?flight ?amount)] db rules from to amount))

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

