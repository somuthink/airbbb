(ns airbbb.time.validation
  (:require
   [clojure.instant :as inst]
   [malli.core :as m]
   [malli.transform :as mt]))

(def registry
  {:time (m/-simple-schema {:type :time, :pred inst?})})

(def date-formatter (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ssX"))

(def transformer
  (mt/transformer
   {:name :time
    :encoders {:time tap>}
    :decoders {:time #(if (instance? java.util.Date %) (.format date-formatter %)
                          (inst/read-instant-date %))}}))
