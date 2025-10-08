(ns airbbb.api.handlers.users
  (:require
   [airbbb.api.handlers.helper :as helper]
   [airbbb.store.interface :as store]
   [airbbb.user.interface :as user]
   [fmnoise.flow :refer [call else then]]
   [malli.util :as mu]))

(defn auth [schema]
  {:openapi {:operationId :create-user}
   :parameters {:body
                (-> schema
                    (mu/select-keys [:user/email])
                    (mu/assoc :user/password :string))}
   :handler
   (fn [{:keys [store secret]
         {:keys [body]} :parameters}]
     (->> (call user/auth-pipe store  secret body)
          (then   (partial assoc
                           {:status 200} :body))
          (else helper/format-fail)))
   :responses {200  {:body :string}}})

(defn create [schema]
  {:openapi {:operationId :create-user}
   :parameters {:body
                (-> schema
                    (mu/dissoc  :user/id)
                    (mu/dissoc  :user/role)
                    (mu/dissoc  :user/tickets)
                    (mu/assoc  :user/password :string))}
   :handler
   (fn [{:keys [store]
         {:keys [body]} :parameters}]
     (->> (call user/create-pipe store body)
          (then   (partial assoc
                           {:status 200} :body))
          (else helper/format-fail)))
   :responses {200  {:body schema}}})

(defn info [schema]
  {:openapi {:operationId :me-user}
   :handler
   (fn [{:keys [user]}]
     {:status 200
      :body (store/e->map user #{:user/password})})
   :responses {200  {:body schema}}})

(defn patch [schema]
  {:openapi {:operationId :patch-user}
   :parameters {:body   (-> schema
                            (mu/select-keys  [:user/name :user/role])
                            mu/optional-keys)}
   :handler
   (fn [{:keys [store user]
         {:keys [body]} :parameters}]

     (->> (call user/patch-pipe store user body)
          (then   (partial assoc
                           {:status 200} :body))
          (else helper/format-fail)))
   :responses {200 {:body schema}}})



