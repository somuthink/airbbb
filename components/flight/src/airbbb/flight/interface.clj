(ns airbbb.flight.interface
  (:require
   [airbbb.flight.pipes :as pipes]))

(defn buy-tickets-pipe [store user-identity identity amount]
  (pipes/buy-tickets store user-identity identity amount))

(defn create-pipe [store data]
  (pipes/create store data))

(defn patch-pipe [store identity data]
  (pipes/patch store identity data))
