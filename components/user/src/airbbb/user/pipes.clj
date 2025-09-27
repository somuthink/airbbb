(ns airbbb.user.pipes
  (:require
   [airbbb.store.interface :as store]
   [airbbb.user.auth :as auth]
   [airbbb.user.prepare :as prepare]
   [fmnoise.flow :refer [call fail-with then then-call]]))

(defn create [{:keys [store-db store-conn]}
              {:keys [user/email] :as data}]
  (->>
   (call #(if-not (store/user-by-email store-db email)
            data
            (fail-with {:msg "user with such email already exists"
                        :data {:code 409}})))
   (then
    prepare/create)
   (then-call (fn [data]  (store/transact store-conn [data])  data))))

(defn auth [{:keys [store-db]}
            secret
            {:keys [user/email] prompted-password :user/password}]
  (->>
   (call store/user-by-email store-db email)
   (then #(if % % (fail-with {:msg "no user with such email"
                              :data {:code 409}})))
   (then-call #(auth/password->claims secret prompted-password %))))

(defn patch [{:keys [store-conn]}
             {eid :db/id}
             data]
  (->>
   data
   (prepare/patch eid)
   (call store/change store-conn eid)
   (then-call #(store/pull-after-tx % eid))))

