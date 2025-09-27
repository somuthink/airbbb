(ns user
  (:require
   [aero.core :as aero]
   [airbbb.api.core]
   [clojure.java.io :as io]
   [integrant.repl :as ig-repl]))

(defn read-config []
  (-> "api/config.edn"
      io/resource
      aero/read-config
      (dissoc :secrets)))

(ig-repl/set-prep! read-config)

(defn start []
  (ig-repl/go))

(defn stop []
  (ig-repl/halt))

(defn reset []
  (ig-repl/reset))

(start)
