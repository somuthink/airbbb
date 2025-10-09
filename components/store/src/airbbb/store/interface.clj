(ns airbbb.store.interface
  (:require
   [airbbb.store.book :as book]
   [airbbb.store.core :as core]
   [airbbb.store.flight :as flight]
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

(defn places-by-names [db place-names]
  (place/by-names db place-names))

(defn place-by-slug [db place-slug]
  (place/by-slug db place-slug))

(defn pull-places-by-flter [db city stars]
  (place/pull-by-filter db city stars))

;; room
(defn room-by-id [db room-id]
  (room/by-id db room-id))

(defn room-by-slug [db place-eid room-slug]
  (room/by-slug db place-eid room-slug))

(defn pull-rooms-by-filter [db place-eids room-types room-num-rooms room-occupancy]
  (room/pull-by-filter db place-eids room-types room-num-rooms room-occupancy))

(defn pull-rooms-by-place-book-start-end [db place-eid book-start book-end]
  (room/pull-by-place-book-start-end db place-eid book-start book-end))

;; books

(defn book-by-id [db book-id]
  (book/by-id db book-id))

(defn book-available? [db room-eid book-start book-end]
  (book/available? db room-eid book-start book-end))

;; flights

(defn flight-by-id [db flight-id]
  (flight/by-id db flight-id))

(defn flight-by-from-to [db flight-from flight-to]
  (flight/by-from-to db flight-from flight-to))

(defn change [conn eid data]
  @(d/transact conn [(assoc data :db/id eid)]))

(defn transact [conn data]
  @(d/transact conn data))

(defn sort-order [sort-cond order s]
  (core/sort-order sort-cond order s))

(defn e->map ([e ignore-keys]
              (core/e->map e ignore-keys))
  ([e] (e->map e [])))

(defn pull-after-tx
  ([tx e]
   (pull-after-tx tx '[*] e))
  ([tx pattern e]
   (d/pull (:db-after tx) pattern e)))

(defn excise [conn eid]
  @(d/transact conn [[:db/retractEntity eid] {:db/excise eid}]))

(defn entity [db e]
  (d/entity db e))


