(ns airbbb.store.room
  (:require
   [datomic.api :as d]))

(defn by-id [db id]
  (d/entity db [:room/id id]))

(defn by-slug [db place-eid slug]
  (d/entity db
            (d/q '[:find ?room .
                   :in $ ?place ?room-slug
                   :where  [?place :place/rooms ?room]
                   [?room :room/slug ?room-slug]] db place-eid slug)))
