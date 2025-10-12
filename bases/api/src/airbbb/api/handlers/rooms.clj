(ns airbbb.api.handlers.rooms
  (:require
   [airbbb.api.handlers.helper :as helper]
   [airbbb.api.middleware :as mw]
   [airbbb.room.interface :as room]
   [airbbb.store.interface :as store]
   [fmnoise.flow :refer [call else then]]
   [malli.util :as mu]))

(def query-filter-schema
  [:map
   [:sort {:default :room/name} [:enum :room/name :room/price]]
   [:order {:default :desc} [:enum :asc :desc]]
   [:place/name {:optional true} [:vector :string]]
   [:room/type {:optional true} [:vector :keyword]]
   [:room/num-rooms {:optional true} [:vector :int]]
   [:room/occupancy {:optional true} [:vector :int]]])

(defn all [schema]
  {:parameters {:query query-filter-schema}
   :handler (fn [{:keys [store]
                  {{place-names :place/name
                    :keys [sort order room/type room/num-rooms room/occupancy]} :query} :parameters}]
              (->> (call room/place-names-query-pipe store sort order place-names type num-rooms occupancy)
                   (then   (partial assoc
                                    {:status 200} :body))
                   (else helper/format-fail)))
   :responses {200 {:body [:vector schema]}}})

(defn place [schema]
  {:parameters {:query (mu/dissoc query-filter-schema :place/name)}
   :handler (fn [{:keys [store place]
                  {{:keys [sort order room/type room/num-rooms room/occupancy]} :query} :parameters}]
              (->> (call room/place-query-pipe store place sort order type num-rooms occupancy)
                   (then   (partial assoc
                                    {:status 200} :body))
                   (else helper/format-fail)))
   :responses {200 {:body [:vector schema]}}})

(defn info [schema]
  {:openapi {:operationId :room-info}
   :handler
   (fn [{:keys [room]}]
     {:status 200
      :body (store/e->map room #{:room/books})})
   :responses {200  {:body schema}}})

(defn create [schema]
  {:middleware [mw/auth-control    [mw/role :admin]]
   :parameters {:body (-> schema
                          (mu/dissoc :room/id)
                          (mu/dissoc :room/slug)
                          (mu/dissoc :rooom/books))}
   :handler (fn [{:keys [store place]
                  {:keys [body]} :parameters}]
              (->> (call room/create-pipe store place body)
                   (then   (partial assoc
                                    {:status 200} :body))
                   (else helper/format-fail)))
   :responses {200 {:body (mu/dissoc schema :room/books)}}})

(defn patch [schema]
  {:openapi {:operationId :patch-room}
   :parameters {:body   (-> schema
                            (mu/dissoc :room/id)
                            (mu/dissoc :room/books)
                            mu/optional-keys)}
   :handler
   (fn [{:keys [store room]
         {:keys [body]} :parameters}]
     (->> (call room/patch-pipe store room body)
          (then   (partial assoc
                           {:status 200} :body))
          (else helper/format-fail)))
   :responses {200 {:body (mu/dissoc schema :room/books)}}})
