(ns airbbb.api.core
  (:require
   [aero.core :as aero]
   [airbbb.api.middleware :as mw]
   [airbbb.api.openapi :as openapi]
   [airbbb.api.routes :as routes]
   [airbbb.store.interface :as store]
   [clojure.edn :as edn]
   [clojure.instant :as inst]
   [clojure.java.io :as io]
   [integrant.core :as ig]
   [malli.core :as mc]
   [malli.registry :as mr]
   [malli.transform :as mt]
   [muuntaja.core :as m]
   [reitit.coercion.malli :as rcm]
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
  (mr/set-default-registry!
   (mr/composite-registry
    (mc/default-schemas)
    {:time (mc/-simple-schema {:type :time, :pred inst?})}))
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
           :coercion (rcm/create
                      (let [time-transformer (mt/transformer
                                              {:name :time
                                               :decoders {:time inst/read-instant-date}
                                               :encoders {:time #(java.util.Date/from %)}})
                            custom-transformer (mt/transformer
                                                mt/strip-extra-keys-transformer
                                                mt/string-transformer
                                                time-transformer)]

                        (-> rcm/default-options
                            (assoc-in  [:transformers :string :default] custom-transformer)
                            (assoc-in  [:transformers :body] {:default rcm/default-transformer-provider
                                                              :formats {"application/json"
                                                                        (mt/transformer
                                                                         mt/strip-extra-keys-transformer
                                                                         mt/json-transformer
                                                                         time-transformer)}})
                            (assoc-in  [:transformers :response] {:default custom-transformer
                                                                  :formats {"application/json"
                                                                            (mt/transformer
                                                                             mt/strip-extra-keys-transformer
                                                                             mt/json-transformer
                                                                             time-transformer)}})
                            (dissoc :lite))))
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
