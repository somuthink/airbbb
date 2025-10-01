(ns airbbb.store.init)

(defn datomic->malli [schema]
  (reduce
   (fn [acc {:keys [db/ident :db/cardinality db/valueType]}]
     (if (#{:user/password :room/books} ident)
       acc
       (conj acc `[~ident
                   ~@(when (or (#{:node/connection  :node/import} ident) (= cardinality :db.cardinality/many)) [{:optional true}])
                   ~(case valueType
                      :db.type/ref (if (= cardinality :db.cardinality/many)
                                     [:vector :any]
                                     [:map {:closed false}])
                      :db.type/uri :string
                      :db.type/float :double
                      :db.type/string :string
                      :db.type/uuid :uuid
                      :db.type/long :int
                      :db.type/keyword :keyword
                      :db.type/boolean :boolean
                      nil)])))
   [:map]
   schema))
