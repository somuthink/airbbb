(ns airbbb.store.room
  (:require
   [datomic.api :as d]))

(defn by-id [db id]
  (d/entity db [:room/id id]))

(defn by-slug [db place-slug slug]
  (d/entity db
            (d/q '[:find ?place
                   :in $ ?place-slug ?room-slug
                   :where  [?place :place/slug ?place-slug]
                   [?place :place/rooms ?room]
                   [?room :room/slug ?room-slug]] db place-slug slug)))
