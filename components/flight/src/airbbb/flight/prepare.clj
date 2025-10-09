(ns airbbb.flight.prepare)

(defn create [data]
  (assoc data :flight/id (random-uuid)))

(defn patch [eid data]
  (assoc data :db/id eid))

(defn buy-ticket [user-eid eid ticket-amount]
  {:db/id user-eid
   :user/tickets (repeatedly ticket-amount (fn []
                                             {:ticket/id (random-uuid)
                                              :ticket/flight eid}))})
