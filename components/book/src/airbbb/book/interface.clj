(ns airbbb.book.interface
  (:require
   [airbbb.book.pipes :as pipes]))

(defn available-pipe [store place start end days]
  (pipes/avilable store place start end days))

(defn create-pipe [store user room data]
  (pipes/create store user room data))
