(ns airbbb.api.handlers.helper
  (:require
   [airbbb.store.interface :as store]))

(def fail-schema {:body [:map [:error :string]
                         [:details :any]]})

(defn format-fail [fail]
  (tap> fail)
  (let [{:keys [code] :or {code 500} :as data} (ex-data fail)]
    {:status code
     :body {:error (ex-message fail)
            :details data}}))

(defn delete [entity-key]
  {:handler
   (fn [{{:keys [store-conn]} :store :as req}]
     (tap> (entity-key req))
     (store/excise store-conn (-> req entity-key :db/id))
     {:status 204
      :body {}})
   :responses {204 {:description "deleted"}}})
