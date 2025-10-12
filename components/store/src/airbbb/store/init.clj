(ns airbbb.store.init)

(defn datomic->malli [schema]
  (reduce
   (fn [acc {:keys [db/ident :db/cardinality db/valueType]}]
     (if (#{:user/password :user/tickets :room/books :book/owner} ident)
       acc
       (conj acc `[~ident
                   ~@(when (or (#{:node/connection  :node/import} ident) (= cardinality :db.cardinality/many)) [{:optional true}])
                   ~(case valueType
                      :db.type/ref (if (= cardinality :db.cardinality/many)
                                     [:vector :any]
                                     [:map {:closed false}])
                      :db.type/instant [:time {:json-schema/format "date-time"}]
                      :db.type/uri :string
                      :db.type/float :double
                      :db.type/string :string
                      :db.type/uuid :uuid
                      :db.type/long [:and {:default (case ident
                                                      :flight/amount 14
                                                      :flight/duration 120
                                                      :flight/price 2500
                                                      2)} :int]
                      :db.type/keyword :keyword
                      :db.type/boolean :boolean
                      nil)])))
   [:map]
   schema))
