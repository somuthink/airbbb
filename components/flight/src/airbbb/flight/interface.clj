(ns airbbb.flight.interface
  (:require
   [airbbb.flight.pipes :as pipes]))

(defn create-pipe [store data]
  (pipes/create store data))

(defn patch-pipe [store identity data]
  (pipes/patch store identity data))
