(ns airbbb.flight.prepare)

(defn create [data]
  (assoc data :flight/id (random-uuid)))

(defn patch [eid data]
  (assoc data :db/id eid))
