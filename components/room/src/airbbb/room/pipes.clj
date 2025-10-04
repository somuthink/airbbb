(ns airbbb.room.pipes
  (:require
   [airbbb.room.prepare :as prepare]
   [airbbb.store.interface :as store]
   [fmnoise.flow :refer [call fail-with then then-call]]))

(defn place-query [{:keys [store-db]}
                   {place-eid :db/id}
                   sort
                   order
                   types
                   num-rooms
                   occupancy]
  (->>
   (call store/pull-rooms-by-filter store-db [place-eid] types num-rooms occupancy)
   (then #(store/sort-order sort order %))))

(defn place-names-query [{:keys [store-db]}
                         sort
                         order
                         place-names
                         types
                         num-rooms
                         occupancy]
  (->>
   (when place-names
     (call store/places-by-names store-db place-names))
   (then-call #(store/pull-rooms-by-filter store-db % types num-rooms occupancy))
   (then #(store/sort-order sort order %))))

(defn create [{:keys [store-db store-conn]}
              {place-eid :db/id place-slug :place/slug}
              data]
  (->>
   (prepare/create place-eid data)
   (call (fn  [{:keys [room/slug] :as prepared}]
           (if-not (store/room-by-slug store-db place-eid slug)
             prepared
             (fail-with {:msg "room in a place with the same slug already exists"
                         :data {:code 409
                                :place/slug place-slug
                                :room/slug slug}}))))
   (then-call (fn [data]  (store/transact store-conn [data])  data))))

(defn patch [{:keys [store-conn]}
             {eid :db/id}
             data]
  (->>
   (prepare/patch eid data)
   (call store/change store-conn eid)
   (then-call #(store/pull-after-tx % '[* {:rooom/books []}] eid))))






