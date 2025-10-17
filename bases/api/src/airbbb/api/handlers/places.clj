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
                        [:sort {:default :place/name} [:enum :place/name :place/stars]]
                        [:order {:default :desc} [:enum :asc :desc]]
                        [:city {:optional true} :string]
                        [:stars {:optional true} [:vector :int]]]}
   :handler (fn [{{:keys [store-db]} :store
                  {{:keys [sort order city stars]} :query} :parameters}]
              (->> (call store/pull-places-by-flter store-db city stars)
                   (then #(store/sort-order sort order %))
                   (then   (partial assoc
                                    {:status 200} :body))
                   (else helper/format-fail)))
   :responses {200 {:body [:vector (mu/dissoc schema :place/rooms)]}}})

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
  {:openapi {:operationId :info-place}
   :handler
   (fn [{:keys [place]}]
     {:status 200
      :body (store/e->map place #{:place/rooms})})
   :responses {200  {:body schema}}})

(defn patch [schema]
  {:openapi {:operationId :patch-place}
   :parameters {:body   (-> schema
                            (mu/dissoc :place/rooms)
                            (mu/dissoc :place/slug)
                            mu/optional-keys)}
   :handler
   (fn [{:keys [store place]
         {:keys [body]} :parameters}]

     (->> (call place/patch-pipe store place body)
          (then   (partial assoc
                           {:status 200} :body))
          (else helper/format-fail)))
   :responses {200 {:body (mu/dissoc schema  :place/rooms)}}})
