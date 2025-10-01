(ns airbbb.api.routes
  (:require
   [airbbb.api.handlers.places :as places-h]
   [airbbb.api.handlers.rooms :as rooms-h]
   [airbbb.api.handlers.users :as users-h]
   [airbbb.api.middleware :as mw]))

(def placeholder-handler (constantly {:status 200}))

(defn routes [{user-schema :user place-schema :place room-schema :room}]
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
      :post (places-h/create place-schema room-schema)
      :get (places-h/all place-schema)}]
    ["/:place-slug"
     [""
      {:tags #{"places"}
       :conflicting true
       :middleware [mw/place-slug->place]
       :parameters {:path [:map [:place-slug :string]]}
       :get (places-h/info place-schema)
       :patch (places-h/patch place-schema)
       :delete (places-h/delete)}]
     ["/:room-slug"
      {:tags #{"rooms"}
       :conflicting true
       :parameters {:path [:map [:room-slug :string]]}
       :middleware [mw/room-slug->room]
       :get (rooms-h/info room-schema)}]]]
   ["/rooms/:room-id"
    [""
     {:tags #{"rooms"}
      :conflicting true
      :parameters {:path [:map [:room-id :uuid]]}
      :middleware [mw/room-id->room]
      :get (users-h/me user-schema)
      :patch (users-h/patch user-schema)
      :delete (users-h/delete)}]
    ["/books"
     {:post placeholder-handler}]]
   ["/books/:book-id"]])
