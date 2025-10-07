(ns airbbb.book.prepare)

(defn create [user-eid room-eid data]
  (assoc data :book/id (random-uuid)
         :room/_books room-eid
         :book/owner user-eid))

