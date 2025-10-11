(ns airbbb.api.handlers.flights
  (:require
   [airbbb.api.handlers.helper :as helper]
   [airbbb.api.middleware :as mw]
   [airbbb.flight.interface :as flight]
   [airbbb.store.interface :as store]
   [fmnoise.flow :refer [call else then]]
   [malli.util :as mu]))

(defn transfers [schema]
  {:parameters {:query [:map [:ticket/amount {:default 1} :int]
                        [:flight/from [:vector :string]]
                        [:flight/to [:vector :string]]
                        [:flight/date {:optional true} [:vector [:time {:json-schema/format "date"}]]]]}
   :handler (fn [{:keys [store]
                  {{ticket-amount :ticket/amount
                    :keys [flight/from flight/to flight/date]} :query} :parameters}]
              (->> (call flight/transfers-pipe store from to ticket-amount date)
                   (then   (partial assoc
                                    {:status 200} :body))
                   (else helper/format-fail)))
   :responses {200 {:body [:vector [:or
                                    [:map
                                     [:path/from :string]
                                     [:path/to :string]
                                     [:path/tags [:set [:enum :cheapest :fastest]]]
                                     [:path/transfers
                                      [:vector
                                       (mu/assoc schema :transfer/item :keyword)]]]
                                    (mu/assoc schema :path/tags  [:set [:enum :cheapest :fastest]])]]}}})

(defn buy-ticket [ticket-schema]
  {:middleware [mw/auth-control]
   :parameters {:body [:map [:ticket/amount {:default 1} :int]]}
   :handler (fn [{:keys [store identity flight]
                  {{ticket-amount :ticket/amount} :body} :parameters}]
              (->> (call flight/buy-tickets-pipe store identity flight ticket-amount)
                   (then   (partial assoc
                                    {:status 200} :body))
                   (else helper/format-fail)))
   :responses {200 {:body [:vector (mu/dissoc ticket-schema :ticket/flight)]}}})

(defn info [schema]
  {:handler
   (fn [{:keys [flight]}]
     {:status 200
      :body (store/e->map flight)})
   :responses {200  {:body schema}}})

(defn create [schema]
  {:middleware [mw/auth-control    [mw/role :admin]]
   :parameters {:body (-> schema
                          (mu/dissoc :flight/id))}
   :handler (fn [{:keys [store]
                  {:keys [body]} :parameters}]
              (->> (call flight/create-pipe store body)
                   (then   (partial assoc
                                    {:status 200} :body))
                   (else helper/format-fail)))
   :responses {200 {:body  schema}}})

(defn patch [schema]
  {:middleware [mw/auth-control    [mw/role :admin]]
   :parameters {:body (-> schema
                          (mu/dissoc :flight/id))}
   :handler (fn [{:keys [store flight]
                  {:keys [body]} :parameters}]
              (->> (call flight/patch-pipe store flight body)
                   (then   (partial assoc
                                    {:status 200} :body))
                   (else helper/format-fail)))
   :responses {200 {:body schema}}})
