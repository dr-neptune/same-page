(ns samepage.server.routes.notes
  (:require [hiccup2.core :refer [html]]
            [samepage.model.model :as note-model]
            [samepage.pages.notes :as notes]))

(defn get-new-note-handler
  [_system request]
  (let [session (:session request)
        user    (:user session)]
    (if user
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (notes/new-note-page request)}
      {:status 302
       :headers {"Location" "/login"}
       :body ""})))

(defn create-note-handler
  [_system request]
  (let [session   (:session request)
        user-name (get-in session [:user :name] "Anonymous")
        note-text (get-in request [:params "note-text"] "")]
    (note-model/create-note! user-name note-text)
    {:status 302
     :headers {"Location" "/"}
     :body ""}))
