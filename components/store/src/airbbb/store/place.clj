(ns airbbb.store.place
  (:require
   [airbbb.store.core :as core]
   [datomic.api :as d]))

(defn by-id [db id]
  (d/entity db [:place/id id]))

(defn by-slug [db slug]
  (d/entity db [:place/slug slug]))

(defn by-names [db names]
  (d/q '[:find [?place ...]
         :in $ [?place-name ...]
         :where [?place :place/name ?place-name]] db names))

(defn pull-by-filter [db city stars]
  (d/query
   (cond->
    '{:find [[(pull ?place [* {:place/rooms []}]) ...]]
      :in [$]
      :where [[?place :place/slug]]}
     true
     (assoc :args [db])
     city
     (->
      (update :in conj '?city)
      (update :args conj city)
      (update :where conj '[?place :place/city ?city]))
     stars
     (->
      (update :in conj '[?stars ...])
      (update :args conj stars)
      (update :where conj '[?place :place/stars ?stars]))
     true
     core/remap-query)))
