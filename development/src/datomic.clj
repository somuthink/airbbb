(ns datomic
  (:require
   [datomic.api :as d]))

(defn conn [] (d/connect "datomic:sql://app?jdbc:sqlite:./storage/sqlite.db"))
