(ns airbbb.user.auth
  (:require
   [buddy.hashers :as hashers]
   [buddy.sign.jwt :as jwt]
   [fmnoise.flow :refer [fail-with]])
  (:import
   (java.time Instant)))

(defn password->claims [secret prompted-password {:keys [user/id user/password]}]
  (if (:valid
       (hashers/verify prompted-password password))
    (jwt/sign {:sub id
               :exp (.getEpochSecond (.plusSeconds (Instant/now) (* 7 24 60 60)))}
              secret)
    (fail-with {:msg "incorrect password or username"
                :data {:code 409}})))

