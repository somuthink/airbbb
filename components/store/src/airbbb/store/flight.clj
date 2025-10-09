(ns airbbb.store.flight
  (:require
   [datomic.api :as d]))

(defn by-id [db id]
  (d/entity db [:flight/id id]))

(defn by-from-to [db from to]
  (d/q '[:find ?flight .
         :in $ ?fligth-from ?flight-to
         :where [?flight :flight/from ?flight-from]
         [?flight :flight/to ?flight-to]] db from to))

