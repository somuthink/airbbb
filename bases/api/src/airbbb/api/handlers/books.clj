(ns airbbb.api.handlers.books
  (:require
   [airbbb.api.handlers.helper :as helper]
   [airbbb.api.middleware :as mw]
   [airbbb.book.interface :as book]
   [fmnoise.flow :refer [call else then]]
   [malli.util :as mu]))

(defn available [schema room-schema]
  (let [start-end-schema (-> schema
                             (mu/select-keys  [:book/start :book/end])
                             mu/optional-keys)]
    {:parameters {:query
                  (-> start-end-schema
                      (mu/assoc :book/days :int)
                      (mu/optional-keys [:book/days]))}
     :handler (fn [{:keys [store place]
                    {{:keys [book/days book/start book/end]} :query} :parameters}]
                (->>
                 (call  book/available-pipe store place start end days)
                 (then   (partial assoc
                                  {:status 200} :body))
                 (else helper/format-fail)))
     :responses {200 {:body [:vector (mu/merge start-end-schema  room-schema)]}}}))

(defn create [schema]
  {:middleware [mw/auth-control]
   :parameters {:body (-> schema
                          (mu/dissoc :book/id)
                          (mu/dissoc :book/owner))}
   :handler (fn [{:keys [store room identity]
                  {:keys [body]} :parameters}]
              (tap> body)
              (->>
               (book/create-pipe store identity room body)
               (then   (partial assoc
                                {:status 200} :body))
               (else helper/format-fail)))
   :responses {200 {:body (mu/dissoc schema :book/owner)}}})
