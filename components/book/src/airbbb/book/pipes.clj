(ns airbbb.book.pipes
  (:require
   [airbbb.book.prepare :as prepare]
   [airbbb.store.interface :as store]
   [fmnoise.flow :refer [call fail-with then then-call]]))

(defn create [{:keys [store-db store-conn]} {user-eid :db/id}
              {room-eid :db/id} {:keys [book/start book/end] :as data}]
  (->>
   (call store/book-available? store-db room-eid start end)
   (then #(if %
            (prepare/create user-eid room-eid data)
            (fail-with {:msg "book on these dates is unavaliable"
                        :data (assoc data :code 400)})))
   (then-call (fn [data]  (store/transact store-conn [data])  data))))

(defn avilable [{:keys [store-db]} {place-eid :db/id} start end days]
  (->>
   (cond
     (and start end (nil? days))
     :start-end
     (and days (every? nil? [start end]))
     :days
     :else
     (fail-with {:msg "either provide book/start with book/end or just book/days"
                 :data {:code 400}}))
   (then-call #(case %
                 :start-end (store/pull-rooms-by-place-book-start-end store-db place-eid start end)
                 :days nil))
   (then #(if (not-empty %) %
              (fail-with {:msg "no rooms available for this book options"
                          :data {:code 404}})))))

(defn patch [{:keys [store-conn]}
             {eid :db/id}
             data]
  (->>
   (call store/change store-conn eid)
   (then-call #(store/pull-after-tx % '[* {:place/rooms []}] eid))))

