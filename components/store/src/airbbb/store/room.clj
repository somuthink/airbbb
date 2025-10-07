(ns airbbb.store.room
  (:require
   [airbbb.store.book :as book]
   [airbbb.store.core :as core]
   [datomic.api :as d]))

(defn pull-by-place-book-start-end [db place-eid book-start book-end]
  (d/q '[:find [(pull ?room [* {:room/books []}]) ...]
         :in $ % ?place ?book-start ?book-end
         :where
         [?place :place/rooms ?room]
         (available ?room ?book-start ?book-end)]
       db book/rules place-eid book-start book-end))

(defn pull-by-filter [db  place-eids types num-rooms occupancy]
  (d/query
   (cond->
    '{:find [[(pull ?room [* {:room/books []}]) ...]]
      :in [$]
      :where [[?room :room/id]]}
     true
     (assoc :args [db])
     place-eids
     (->
      (update :in conj '[?place ...])
      (update :args conj place-eids)
      (update :where conj '[?place :place/rooms ?room]))
     types
     (->
      (update :in conj '[?types ...])
      (update :args conj types)
      (update :where conj '[?room :room/type ?types]))
     num-rooms
     (->
      (update :in conj '[?num-roooms ...])
      (update :args conj num-rooms)
      (update :where conj '[?room :room/num-rooms ?num-rooms]))
     occupancy
     (->
      (update :in conj  '[?occupancy ...])
      (update :args conj occupancy)
      (update :where conj '[?room :room/occupancy ?occupancy]))
     true
     core/remap-query)))

(defn by-id [db id]
  (d/entity db [:room/id id]))

(defn by-slug [db place-eid slug]
  (d/entity db
            (d/q '[:find ?room .
                   :in $ ?place ?room-slug
                   :where  [?place :place/rooms ?room]
                   [?room :room/slug ?room-slug]] db place-eid slug)))
