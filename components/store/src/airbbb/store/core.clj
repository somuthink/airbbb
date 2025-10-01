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

