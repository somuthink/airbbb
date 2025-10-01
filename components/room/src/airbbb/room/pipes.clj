(ns airbbb.room.pipes
  (:require
   [airbbb.room.prepare :as prepare]
   [airbbb.store.interface :as store]
   [fmnoise.flow :refer [call fail-with then-call]]))

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






