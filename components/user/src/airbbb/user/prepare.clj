(ns airbbb.user.prepare
  (:require
   [buddy.hashers :as hashers]))

(defn patch [eid data]
  (assoc data :db/id eid))

(defn create [data]
  (-> data
      (assoc
       :user/id (random-uuid)
       :user/role :user)
      (update :user/password hashers/derive)))
