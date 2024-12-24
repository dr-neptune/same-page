(ns samepage.routes
  (:require [reitit.ring :as reitit-ring]
            [samepage.pages :as pages]
            [samepage.model :as model]
            [hiccup.core :refer [html]]))

;; root-page handler
(defn root-page-handler
  [system request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (pages/root-page)})

;; new-note-page handler
(defn new-note-handler
  [system request]
  (let [notes (model/get-session-notes (:session request))]
    {:status  200
     :headers {"Content-Type" "text/html"}
     :body    (pages/new-note-page notes)}))

;; HTMX endpoint
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

    ["/create-notes"
     {:get {:handler (partial #'new-note-handler system)}}]

    ["/notes"
     {:post {:handler (partial #'create-note-handler system)}}]
    ;; Removed single-note route
  ])

(defn root-handler
  [system request]
  (let [handler (reitit-ring/ring-handler
                  (reitit-ring/router (routes system))
                  #'not-found-handler)]
    (handler request)))
