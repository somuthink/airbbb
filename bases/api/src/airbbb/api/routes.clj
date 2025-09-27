(ns airbbb.api.routes
  (:require
   [airbbb.api.handlers.users :as users-h]
   [airbbb.api.middleware :as mw]))

(def placeholder-handler (constantly {:status 200}))

(defn routes [{user-schema :user}]
  [["/auth"
    {:tags #{"auth"}
     :include-secret true
     :post (users-h/auth user-schema)}]
   ["/users"
    {:tags #{"users"}}
    [""
     {:post (users-h/create user-schema)}]
    ["/me"
     {:conflicting true
      :middleware [mw/auth-control]
      :get (users-h/me user-schema)
      :patch (users-h/patch user-schema)
      :delete (users-h/delete)}]]
   ["/places"
    [""
     {:tags #{"places"}
      :post (users-h/create user-schema)
      :get (users-h/create user-schema)}]
    ["/:place-id"
     {:tags #{"places"}
      :conflicting true
      :parameters {:path [:map [:place-id :uuid]]}
      :middleware [mw/auth-control]
      :get (users-h/me user-schema)
      :patch (users-h/patch user-schema)
      :delete (users-h/delete)}]
    ["/:place-slug"
     [""
      {:tags #{"places"}
       :conflicting true
       :parameters {:path [:map [:room-slug :string]]}
       :middleware [mw/auth-control]
       :get (users-h/me user-schema)
       :patch (users-h/patch user-schema)
       :delete (users-h/delete)}]
     ["/:room-slug"
      {:tags #{"rooms"}
       :conflicting true
       :parameters {:path [:map [:room-slug :string]]}
       :middleware [mw/auth-control]
       :get (users-h/me user-schema)
       :patch (users-h/patch user-schema)
       :delete (users-h/delete)}]]]
   ["/rooms/:room-id"
    {:tags #{"rooms"}
     :conflicting true
     :parameters {:path [:map [:room-id :uuid]]}
     :middleware [mw/auth-control]
     :get (users-h/me user-schema)
     :patch (users-h/patch user-schema)
     :delete (users-h/delete)}]])
