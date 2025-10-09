(ns airbbb.api.openapi
  (:require
   [reitit.openapi :as openapi]
   [reitit.swagger-ui :as swagger-ui]))

(def json
  ["/docs/openapi.json"
   {:get {:no-doc true
          :openapi {:info {:title "airbbb-api"
                           :description "openapi3 docs with malli"
                           :version "0.0.1"}
                    :components {:securitySchemes {"auth" {:type :http
                                                           :scheme :bearer
                                                           :in :header
                                                           :name "auth"}}}
                    :security [{"auth" []}]}
          :handler (openapi/create-openapi-handler)}}])

(def ui-handler (swagger-ui/create-swagger-ui-handler
                 {:path "/docs"
                  :config {:validatorUrl nil
                           :urls [{:name "openapi", :url "openapi.json"}]
                           :urls.primaryName "openapi"
                           :operationsSorter "alpha"}}))
