(ns airbbb.place.interface
  (:require
   [airbbb.place.pipes :as pipes]))

(defn create-pipe [store data]
  (pipes/create store data))

(defn patch-pipe [store identity data]
  (pipes/patch store identity data))
