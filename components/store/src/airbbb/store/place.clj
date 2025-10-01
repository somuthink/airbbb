(ns airbbb.store.place
  (:require
   [datomic.api :as d]))

(defn by-id [db id]
  (d/entity db [:place/id id]))

(defn by-slug [db slug]
  (d/entity db [:place/slug slug]))

(defn pull-by-filter [db city stars]
  (d/q  '[:find (pull ?place [* {:place/rooms []}])
          :in $ ?city [?stars ...]
          :where [?place :place/city city]
          [?place :place/stars stars]]
        db city stars))
