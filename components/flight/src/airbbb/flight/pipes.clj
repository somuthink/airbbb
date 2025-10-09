(ns airbbb.flight.pipes
  (:require
   [airbbb.flight.prepare :as prepare]
   [airbbb.store.interface :as store]
   [fmnoise.flow :refer [call fail-with then then-call]]))

(defn create [{:keys [store-db store-conn]} {:keys [flight/from flight/to] :as data}]
  (->>
   (call store/flight-by-from-to store-db from to)
   (then #(if-not %
            (prepare/create data)
            (fail-with {:msg "flight with the same from and to already exists"
                        :data {:code 409
                               :flight/from from
                               :flight/to to}})))
   (then-call (fn [data] (store/transact store-conn [data]) data))))

(defn patch [{:keys [store-conn]}
             {eid :db/id}
             data]
  (->>
   (prepare/patch eid data)
   (call store/change store-conn eid)
   (then-call #(store/pull-after-tx % eid))))
