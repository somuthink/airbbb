(ns airbbb.user.interface
  (:require
   [airbbb.user.pipes :as pipes]))

(defn auth-pipe [store secret data]
  (pipes/auth store secret data))

(defn create-pipe [store data]
  (pipes/create store data))

(defn patch-pipe [store identity data]
  (pipes/patch store identity data))
