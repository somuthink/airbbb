(ns airbbb.store.book
  (:require
   [datomic.api :as d]))

(defn by-id [db id]
  (d/entity db [:book/id id]))

(def rules
  '[[(available ?room ?pred-book-start ?pred-book-end)
     (or-join [?room ?pred-book-start ?pred-book-end]
              (not [?room :room/books _])
              (not-join [?room ?pred-book-start ?pred-book-end]
                        [?room :room/books ?book]
                        [?book :book/start ?book-start]
                        [?book :book/end ?book-end]
                        [(<= ?book-start ?pred-book-end)]
                        [(>= ?book-end ?pred-book-start)]))]])

(defn available? [db room-eid start end]
  (not
   (tap>
    (d/q '[:find ?room .
           :in $ % ?room ?book-start ?book-end
           :where (available ?room ?book-start ?book-end)] db rules room-eid start end))))




