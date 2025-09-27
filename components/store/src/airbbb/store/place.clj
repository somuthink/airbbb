(ns airbbb.store.place
  (:require
   [datomic.api :as d]))

(defn by-id [db id]
  (d/entity db [:place/id id]))

(defn by-slug [db slug]
  (d/entity db [:place/slug slug]))
