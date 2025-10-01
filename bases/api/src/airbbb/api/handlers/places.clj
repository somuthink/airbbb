(ns airbbb.api.handlers.places
  (:require
   [airbbb.api.handlers.helper :as helper]
   [airbbb.api.middleware :as mw]
   [airbbb.place.interface :as place]
   [airbbb.store.interface :as store]
   [fmnoise.flow :refer [call else then]]
   [malli.util :as mu]))

(defn all [schema]
  {:parameters {:query [:map
                        [:city :string]
                        [:stars [:vector :int]]]}
   :handler (fn [{{:keys [store-db]} :store
                  {{:keys [city stars]} :query} :parameters}]
              (->> (call store/pull-place-by-flter store-db  city stars)
                   (then   (partial assoc
                                    {:status 200} :body))
                   (else helper/format-fail)))
   :responses {200 {:body [:vector schema]}}})

(defn create [schema room-schema]
  {:middleware [mw/auth-control [mw/role :admin]]
   :parameters {:body (-> schema
                          (mu/dissoc :place/id)
                          (mu/dissoc :place/slug)
                          (mu/assoc :place/rooms [:vector (-> room-schema
                                                              (mu/dissoc :room/slug)
                                                              (mu/dissoc  :room/id))]))}
   :handler (fn [{:keys [store]
                  {:keys [body]} :parameters}]
              (->> (call place/create-pipe store body)
                   (then   (partial assoc
                                    {:status 200} :body))
                   (else helper/format-fail)))
   :responses {200 {:body (mu/assoc schema :place/rooms [:vector room-schema])}}})

(defn info [schema]
  {:openapi {:operationId :me-user}
   :handler
   (fn [{:keys [place]}]
     {:status 200
      :body (store/e->map place)})
   :responses {200  {:body schema}}})

(defn delete []
  {:openapi {:operationId :delete-place}
   :handler
   (fn [{{:keys [store-conn]} :store
         {eid :db/id} :place}]
     (store/excise store-conn eid)
     {:status 204
      :body {}})
   :responses {204 {:description "deleted"}}})

(defn patch [schema]
  {:openapi {:operationId :patch-place}
   :parameters {:body   (-> schema
                            (mu/dissoc :place/rooms)
                            mu/optional-keys)}
   :handler
   (fn [{:keys [store place]
         {:keys [body]} :parameters}]

     (->> (call place/patch-pipe store place body)
          (then   (partial assoc
                           {:status 200} :body))
          (else helper/format-fail)))
   :responses {200 {:body schema}}})
