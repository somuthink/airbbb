(ns airbbb.flight.interface
  (:require
   [airbbb.flight.pipes :as pipes]))

(defn transfers-pipe [store from to amount date]
  (pipes/transfers store from to amount date))

(defn buy-tickets-pipe [store user-identity identity amount]
  (pipes/buy-tickets store user-identity identity amount))

(defn create-pipe [store data]
  (pipes/create store data))

(defn patch-pipe [store identity data]
  (pipes/patch store identity data))
