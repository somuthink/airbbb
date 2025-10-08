(ns airbbb.time.interface
  (:require
   [airbbb.time.days :as days]
   [airbbb.time.validation :as validation]))

(defn days-diff [from to]
  (days/diff from to))

(defn add-days [time num-days]
  (days/add time num-days))

(def validation-registry validation/registry)
(def validation-transformer validation/transformer)
