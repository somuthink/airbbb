(ns airbbb.place.pipes
  (:require
   [airbbb.place.prepare :as prepare]
   [airbbb.store.interface :as store]
   [fmnoise.flow :refer [call fail-with then-call]]))

(defn create [{:keys [store-db store-conn]}
              data]
  (->>
   (prepare/create data)
   (call (fn  [{:keys [place/slug] :as prepared}]
           (if-not (store/place-by-slug store-db slug)
             prepared
             (fail-with {:msg "place with the same slug already exists"
                         :data {:code 409
                                :slug slug}}))))
   (then-call (fn [data]  (store/transact store-conn [data])  data))))

(defn patch [{:keys [store-conn]}
             {eid :db/id}
             data]
  (->>
   (prepare/patch eid data)
   (call store/change store-conn eid)
   (then-call #(store/pull-after-tx % eid))))
