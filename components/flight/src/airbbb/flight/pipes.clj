(ns airbbb.flight.pipes
  (:require
   [airbbb.flight.prepare :as prepare]
   [airbbb.store.interface :as store]
   [fmnoise.flow :refer [call fail-with then then-call]]))

(defn buy-tickets [{:keys [store-db store-conn]}
                   {user-eid :db/id}
                   {eid :db/id :keys [flight/amount]} ticket-amount]
  (->>
   (call store/flight-available? store-db eid ticket-amount)
   (then #(if %
            (prepare/buy-ticket user-eid eid ticket-amount)
            (fail-with {:msg "no space rest on this flight"
                        :data {:code 409
                               :flight/amount amount}})))
   (then-call (fn [{:keys [user/tickets] :as data}] (store/transact store-conn [data]) tickets))))

(defn create [{:keys [store-db store-conn]}
              {:keys [flight/from flight/to] :as data}]
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
