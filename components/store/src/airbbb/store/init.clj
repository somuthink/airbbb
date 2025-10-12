(ns airbbb.store.init
  (:require
   [datomic.api :as d]))

(defn- connect [uri]
  (try
    (d/connect uri)
    (catch Exception e (println e))))

(defn wait-for
  [uri & [{:keys [retries sleep-ms]
           :or {retries 20 sleep-ms 2000}}]]
  (loop [attempt 1]
    (println (format "attempting datomic connect (%d/%d)..." attempt retries))
    (if-let [conn (connect uri)]
      (do
        (println "connected to datomoc transactor")
        conn)
      (if (< attempt retries)
        (recur (do (Thread/sleep sleep-ms) (inc attempt)))
        (throw (ex-info "failed to connect to datomic after retries." {}))))))

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
