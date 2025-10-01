(ns airbbb.place.prepare
  (:require
   [airbbb.room.interface :as room]
   [sluj.core :refer [sluj]]))

(defn create [{:keys [place/name place/rooms] :as data}]
  (assoc data
         :place/slug (sluj name)
         :place/rooms (map room/prepare-create rooms)))

(defn patch [eid {:keys [place/name] :as data}]
  (assoc data
         :db/id eid
         :place/slug (sluj name)))
