(ns airbbb.room.prepare
  (:require
   [sluj.core :refer [sluj]]))

(defn create  [place-eid {:keys [room/name] :as data}]
  (cond-> data
    true (assoc :room/id (random-uuid)
                :room/slug (sluj name))
    place-eid (assoc :place/_rooms place-eid)))

(defn patch [eid {:keys [room/name] :as data}]
  (as-> data d
    (assoc d :db/id eid)
    (if name (assoc d :room/slug (sluj name)) d)))
