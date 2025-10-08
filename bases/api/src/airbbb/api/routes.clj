(ns airbbb.api.routes
  (:require
   [airbbb.api.handlers.books :as books-h]
   [airbbb.api.handlers.helper :as helper-h]
   [airbbb.api.handlers.places :as places-h]
   [airbbb.api.handlers.rooms :as rooms-h]
   [airbbb.api.handlers.users :as users-h]
   [airbbb.api.middleware :as mw]))

(def placeholder-handler (constantly {:status 200}))

(defn routes [{user-schema :user
               place-schema :place
               room-schema :room
               book-schema :book}]
  [["/auth"
    {:tags #{"auth"}
     :include-secret true
     :post (users-h/auth user-schema)}]
   ["/users"
    {:tags #{"users"}}
    [""
     {:conflicting true
      :post (users-h/create user-schema)}]
    ["/:user-identity"
     {:parameters {:path [:map [:user-identity {:default :me} :string]]}
      :middleware [mw/auth-control mw/user-identity->user]
      :get (users-h/info user-schema)
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
        :get (rooms-h/place room-schema)
        :post (rooms-h/create room-schema)}]
      ["/:room-slug"
       {:conflicting true
        :parameters {:path [:map [:room-slug :string]]}
        :middleware [mw/room-slug->room]
        :get (rooms-h/info room-schema)}]]
     ["/books/available"
      {:tags #{"books"}
       :get (books-h/available book-schema room-schema)}]]]
   ["/rooms"
    [""
     {:tags #{"rooms"}
      :get (rooms-h/all room-schema)}]
    ["/:room-id"
     {:conflicting true
      :parameters {:path [:map [:room-id :uuid]]}
      :middleware [mw/room-id->room]}
     [""
      {:tags #{"rooms"}
       :conflicting true
       :get (rooms-h/info room-schema)
       :patch (rooms-h/patch room-schema)
       :delete (helper-h/delete :room)}]
     ["/books"
      {:tags #{"books"}
       :get (books-h/room-infos book-schema)
       :post (books-h/create book-schema)}]]]

   ["/books/:book-id"
    {:tags #{"books"}
     :parameters {:path [:map [:book-id :uuid]]}
     :middleware [mw/auth-control mw/book-id->book]
     :delete (helper-h/delete :book)}]])
