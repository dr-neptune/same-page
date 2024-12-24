(ns samepage.server.routes
  (:require [reitit.ring :as reitit-ring]
            [hiccup.core :refer [html]]
            [samepage.model.model :as model]
            [samepage.pages.pages :as pages]))

;; Landing page
(defn root-page-handler
  [system request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (pages/root-page)})

;; GET /register
(defn get-register-handler
  [system request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (pages/register-page)})

;; POST /register
(defn post-register-handler
  [system request]
  (let [user-name (get-in request [:params "user-name"] "Anonymous")]
    {:status  302
     :session (assoc (:session request) :user {:name user-name})
     :headers {"Location" "/create-notes"}
     :body    ""}))

;; GET /create-notes
(defn new-note-handler
  [system request]
  (let [session   (:session request)
        user-name (get-in session [:user :name] "Guest")
        notes     (model/get-session-notes session)]
    {:status  200
     :headers {"Content-Type" "text/html"}
     :body    (pages/new-note-page notes user-name)}))

;; POST /notes (HTMX partial update)
(defn create-note-handler
  [system request]
  (let [session     (:session request)
        note-text   (get-in request [:params "note-text"] "")
        new-session (model/add-session-note session note-text)
        notes       (model/get-session-notes new-session)]
    {:status  200
     :headers {"Content-Type" "text/html"}
     :session new-session
     :body    (html (pages/notes-table notes))}))

(defn not-found-handler
  [_request]
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body    "Not Found"})

(defn routes
  [system]
  [ ["/"
     {:get {:handler (partial #'root-page-handler system)}}]
    ["/register"
     {:get  {:handler (partial #'get-register-handler system)}
      :post {:handler (partial #'post-register-handler system)}}]
    ["/create-notes"
     {:get {:handler (partial #'new-note-handler system)}}]
    ["/notes"
     {:post {:handler (partial #'create-note-handler system)}}] ])

(defn root-handler
  "Given a `system` and a request, run the Reitit ring-handler."
  [system request]
  (let [handler (reitit-ring/ring-handler
                 (reitit-ring/router (routes system))
                 not-found-handler)]
    (handler request)))
