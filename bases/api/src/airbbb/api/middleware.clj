(ns airbbb.api.middleware
  (:require
   [airbbb.api.handlers.helper :as helper]
   [airbbb.store.interface :as store]
   [buddy.sign.jwt :as jwt]
   [fmnoise.flow :refer [call else fail-with then]]
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
                          (assoc request :identity)))
              (then handler)
              (else (constantly {:status 401
                                 :body {:msg "bad auth token"}})))))})

(def role
  {:name ::role
   :description "check if user satisfies role"
   :wrap (fn [handler role]
           (fn [{:keys [identity] :as request}]
             (if (= role (:user/role identity))
               (handler request)
               {:status 401
                :body "no access to the resourse"})))})

(def databases
  {:name ::databases
   :description "Middleware that adds store to req"
   :wrap (fn [handler store-conn]
           (fn [request]
             (handler
              (assoc request :store {:store-conn store-conn
                                     :store-db (store/create-db store-conn)}))))})

(def place-slug->place
  {:name ::project-slug->place
   :description "lookups the place by slug "
   :wrap (fn [handler]
           (fn [{{:keys [store-db]} :store
                 {{:keys [place-slug]} :path} :parameters
                 :as request}]
             (->>
              (call store/place-by-slug store-db  place-slug)
              (then #(if %
                       (handler (assoc request :place %))
                       {:status 404
                        :body {:error "no place with such slug" :details {:place/slug place-slug}}}))
              (else helper/format-fail))))})

(def room-slug->room
  {:name ::room-slug->room
   :description "lookups the room by rooom and place slug"
   :wrap (fn [handler]
           (fn [{{:keys [store-db]} :store
                 {{:keys [room-slug]} :path} :parameters
                 {place-eid :db/id :as place} :place
                 :as request}]
             (->>
              (call store/room-by-slug store-db place-eid room-slug)
              (then #(if %
                       (handler (assoc request :room %))
                       {:status 404
                        :body {:error "no room with such slug" :details {:room/slug room-slug}}}))
              (else helper/format-fail))))})

(def room-id->room
  {:name ::room-id->room
   :description "lookups the  room by id"
   :wrap (fn [handler]
           (fn [{{:keys [store-db]} :store
                 {{:keys [room-id]} :path} :parameters
                 :as request}]
             (->>
              (call store/room-by-id store-db  room-id)
              (then #(if %
                       (handler (assoc request :room %))
                       {:status 401
                        :body {:error "no room with such id" :details {:room/id room-id}}}))
              (else helper/format-fail))))})

(def book-id->book
  {:name ::book-id->book
   :description "lookups the  room by id"
   :wrap (fn [handler]
           (fn [{{:keys [store-db]} :store
                 {user-eid :db/id user-role :user/role} :identity
                 {{:keys [book-id]} :path} :parameters
                 :as request}]
             (->>

              (call store/book-by-id store-db  book-id)
              (then #(cond
                       (or
                        (= user-role :admin)
                        (= user-eid (:book/owner %)))
                       (handler (assoc request :book %))
                       %
                       {:status 401
                        :body {:error "no access to this book" :details {:book/id book-id}}}
                       :else
                       {:status 404
                        :body {:error "no book with such id" :details {:book/id book-id}}}))
              (else helper/format-fail))))})

(def user-identity->user
  {:name ::user-identity->user
   :description "lookups the user affectetd by operation by its identity"
   :wrap (fn [handler]
           (fn [{{:keys [store-db]} :store
                 {user-role :user/role :as user} :identity
                 {{:keys [user-identity]} :path} :parameters
                 :as request}]
             (if (= user-identity "me")
               (handler (assoc request :user user))
               (->>
                (call #(if (= user-role :admin)
                         (store/user-by-id store-db (parse-uuid user-identity))
                         (fail-with {:msg "only admins can use other users identity"
                                     :data {:code 409}})))
                (then #(if  %
                         (handler (assoc request :user %))
                         {:status 404
                          :body {:error "no user with such id" :details {:user/id user-identity}}}))
                (else helper/format-fail)))))})

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
