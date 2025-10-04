(ns airbbb.room.interface
  (:require
   [airbbb.room.pipes :as pipes]
   [airbbb.room.prepare :as prepare]))

(defn place-query-pipe [store place sort order types num-rooms occupancy]
  (pipes/place-query store place sort order types num-rooms occupancy))

(defn place-names-query-pipe [store sort order place-names types num-rooms occupancy]
  (pipes/place-names-query store sort order place-names types num-rooms occupancy))

(defn prepare-create
  ([place-eid data]
   (prepare/create place-eid data))
  ([data]
   (prepare/create nil data)))

(defn create-pipe [store place data]
  (pipes/create store place data))

(defn patch-pipe [store identity data]
  (pipes/patch store identity data))
