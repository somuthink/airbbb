(ns airbbb.store.core
  (:require
   [clojure.walk :as w]))

(defn e->map
  [e ignore-keys]
  (w/prewalk
   (fn [x]
     (if (instance? datomic.query.EntityMap x)
       (apply dissoc
              (into {} x) ignore-keys)
       x))
   e))

(defn remap-query
  [{args :args :as m}]
  {:query (dissoc m :args)
   :args args})

(defn sort-order [sort-cond order s]
  (sort-by
   sort-cond
   (case order :asc #(compare %1 %2) :desc #(compare %2 %1))
   s))
