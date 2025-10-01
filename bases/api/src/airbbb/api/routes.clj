(ns airbbb.api.routes
  (:require
   [airbbb.api.handlers.helper :as helper-h]
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
      :delete (helper-h/delete :identity)}]]
   ["/places"
    [""
     {:tags #{"places"}
      :post (places-h/create place-schema room-schema)
      :get (places-h/all place-schema)}]
    ["/:place-slug"
     {:parameters {:path [:map [:place-slug :string]]}
      :middleware [mw/place-slug->place]}
     [""
      {:tags #{"places"}
       :conflicting true
       :get (places-h/info place-schema)
       :patch (places-h/patch place-schema)
       :delete (helper-h/delete :place)}]
     ["/rooms"
      {:tags #{"rooms"}}
      [""
       {:conflicting true
        :post (rooms-h/create room-schema)}]
      ["/:room-slug"
       {:conflicting true
        :parameters {:path [:map [:room-slug :string]]}
        :middleware [mw/room-slug->room]
        :get (rooms-h/info room-schema)}]]]]
   ["/rooms/:room-id"
    {:middleware [mw/room-id->room]}
    [""
     {:tags #{"rooms"}
      :conflicting true
      :parameters {:path [:map [:room-id :uuid]]}
      :get (rooms-h/info room-schema)
      :patch (rooms-h/patch room-schema)
      :delete (helper-h/delete :room)}]
    ["/books"
     {:post placeholder-handler}]]
   ["/books/:book-id"]])
