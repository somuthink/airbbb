(ns airbbb.api.middleware
  (:require
   [airbbb.store.interface :as store]
   [buddy.sign.jwt :as jwt]
   [fmnoise.flow :refer [call else]]
   [reitit.coercion.malli]
   [reitit.openapi :as openapi]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.malli]
   [reitit.ring.middleware.exception :as exception]
   [reitit.ring.middleware.multipart :as multipart]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]
   [reitit.swagger :as swagger]))

(def auth-control
  {:name ::auth-control
   :description "checks the auth and acces  "
   :wrap (fn [handler]
           (fn [{{:keys [store-db]} :store
                 :keys [secret headers]
                 :as request}]
             (->>
              (call #(->> (jwt/unsign (subs (get headers "authorization") 7) secret)
                          :sub
                          parse-uuid
                          (store/user-by-id store-db)
                          (assoc request :identity)
                          handler))
              (else {:status 401
                     :body "bad auth token"}))))})

(def databases
  {:name ::databases
   :description "Middleware that adds store to req"
   :wrap (fn [handler store-conn]
           (fn [request]
             (handler
              (assoc request :store {:store-conn store-conn
                                     :store-db (store/create-db store-conn)}))))})

(def secret
  {:name ::secret
   :description "Middleware that adds jwt secret to req"
   :wrap (fn [handler secret]
           (fn [request]
             (handler
              (assoc request :secret secret))))})

(defn exception-handler [message exception request]
  {:status 500
   :body {:error {:message message
                  :exception (.getClass exception)
                  :data (ex-data exception)
                  :uri (:uri request)}}})

(def exception
  (exception/create-exception-middleware
   (merge
    exception/default-handlers
    {;; ex-data with :type ::error
     ::error (partial exception-handler "error")

       ;; ex-data with ::exception or ::failure
     ::exception (partial exception-handler "exception")

       ;; override the default exception-handler
     ::exception/default (partial exception-handler "default")

       ;; print stack-traces for all exceptions
     ::exception/wrap (fn [handler e request]
                        (tap> e)
                        (handler e request))})))

(defn middlewares [{:keys [store-conn jwt-secret]}]
  [;; 2. Swagger and OpenAPI
   swagger/swagger-feature
   openapi/openapi-feature

   ;; 3. Params, formats, multipart
   parameters/parameters-middleware
   muuntaja/format-negotiate-middleware
   muuntaja/format-response-middleware
   muuntaja/format-request-middleware
   multipart/multipart-middleware

   ;; 4. Exception handling
   exception

   ;; 5. Request/Response coercion
   coercion/coerce-response-middleware
   coercion/coerce-request-middleware

   ;; 6. Stores
   [databases store-conn]

   [secret jwt-secret]])
