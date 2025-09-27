(ns airbbb.store.user
  (:require
   [datomic.api :as d]))

(defn by-id [db id]
  (d/entity db [:user/id id]))

(defn by-email [db email]
  (d/entity db [:user/email email]))
