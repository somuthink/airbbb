(ns airbbb.store.interface
  (:require
   [airbbb.store.core :as core]
   [airbbb.store.init :as init]
   [airbbb.store.place :as place]
   [airbbb.store.room :as room]
   [airbbb.store.user :as user]
   [datomic.api :as d]))

(defn store->validate [schema]
  (init/datomic->malli schema))

(defn create-conn [uri]
  (d/connect uri))

(defn init-schema [conn schema]
  @(d/transact conn schema))

(defn create-db [conn]
  (d/db conn))

;; user

(defn user-by-id [db user-id]
  (user/by-id db user-id))

(defn user-by-email [db user-email]
  (user/by-email db user-email))

;; place

(defn place-by-id [db place-id]
  (place/by-id db place-id))

(defn place-by-slug [db place-slug]
  (place/by-slug db place-slug))

(defn pull-place-by-flter [db sort-cond order city stars]
  (place/pull-by-filter db sort-cond order city stars))

;; room
(defn room-by-id [db room-id]
  (room/by-id db room-id))

(defn room-by-slug [db place-eid room-slug]
  (room/by-slug db place-eid room-slug))

(defn change [conn eid data]
  @(d/transact conn [(assoc data :db/id eid)]))

(defn transact [conn data]
  @(d/transact conn data))

(defn e->map [e]
  (core/e->map e))

(defn pull-e [db {:keys [db/id]}]
  (d/pull db '[] id))

(defn pull-after-tx
  ([tx e]
   (pull-after-tx tx '[*] e))
  ([tx pattern e]
   (d/pull (:db-after tx) pattern e)))

(defn excise [conn eid]
  @(d/transact conn [[:db/retractEntity eid] {:db/excise eid}]))

(defn entity [db e]
  (d/entity db e))


