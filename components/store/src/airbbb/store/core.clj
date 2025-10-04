(ns airbbb.store.core)

(defn e->map
  [e]
  (into {}
        (map (fn [[k v]]
               (cond

                 (instance? datomic.query.EntityMap v)
                 [k (e->map v)]

                 (and (coll? v)
                      (every? #(instance? datomic.query.EntityMap %) v))
                 [k (mapv e->map v)]

                 :else [k v])))
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
