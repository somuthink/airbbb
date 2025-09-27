(ns airbbb.api.core
  (:require
   [aero.core :as aero]
   [airbbb.api.middleware :as mw]
   [airbbb.api.openapi :as openapi]
   [airbbb.api.routes :as routes]
   [airbbb.store.interface :as store]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [integrant.core :as ig]
   [malli.util :as mu]
   [muuntaja.core :as m]
   [reitit.coercion.malli]
   [reitit.dev.pretty :as pretty]
   [reitit.ring :as ring]
   [reitit.ring.spec :as spec]
   [ring.adapter.jetty :as jetty]))

(defmethod ig/init-key :schema/load [_ {:keys [resource-prefix]}]
  (-> (str  resource-prefix  ".edn")
      io/resource
      slurp
      edn/read-string))

(defmethod ig/init-key :schema/validation [_ store-schema]
  (update-vals
   store-schema
   store/store->validate))

(defmethod ig/init-key :db/datomic [_ {:keys [uri store-schema]}]
  (let [schema-data (->>
                     store-schema
                     vals
                     (apply concat)
                     vec)
        conn (store/create-conn uri)]
    (store/init-schema conn schema-data)
    conn))

(defmethod ig/init-key :router/routes [_ schema]
  (into ["/api"  (routes/routes schema)] [openapi/json]))

(defmethod ig/init-key :app/router [_ {:keys [routes] :as opts}]
  (ring/router
   routes
   {:validate spec/validate
    :exception pretty/exception
    :data {:muuntaja m/instance
           :coercion (reitit.coercion.malli/create
                      {:compile mu/closed-schema
                       :strip-extra-keys true
                       :default-values true
                       :options nil})
           :middleware (-> opts
                           (dissoc :routes)
                           mw/middlewares)}}))

(defmethod ig/init-key :app/handler [_ {:keys [router]}]
  (ring/ring-handler
   router
   (ring/routes
    openapi/ui-handler
    (ring/create-default-handler))))

(defmethod ig/init-key :server/http [_ {:keys [handler port]}]
  (jetty/run-jetty handler {:port port :join? false}))

(defmethod ig/halt-key! :server/http [_ server]
  (.stop server))

(defmethod aero/reader 'ig/ref
  [_ _ value]
  (ig/ref value))

(defn -main []
  (->
   "api/config.edn"
   io/resource
   aero/read-config
   ig/init))
