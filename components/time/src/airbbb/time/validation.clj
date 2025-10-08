(ns airbbb.time.validation
  (:require
   [clojure.instant :as inst]
   [malli.core :as m]
   [malli.transform :as mt]))

(def registry
  {:time (m/-simple-schema {:type :time, :pred inst?})})

(def transformer
  (mt/transformer
   {:name :time
    :decoders {:time inst/read-instant-date}
    :encoders {:time #(java.util.Date/from %)}}))
