(ns samepage.routes
  (:require [reitit.ring :as reitit-ring]
            [samepage.pages :as pages]
            [samepage.model :as model]))

(defn root-page-handler
  [_system _request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "<p>Go to <a href=\"/create-notes\">/create-notes</a> to create a note.</p>"})

(defn new-note-handler
  [_system _request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body    (pages/new-note-page)})

(defn create-note-handler
  [_system request]
  (let [note-text (get-in request [:params "note-text"] "")
        note-id   (model/create-note! note-text)]
    {:status  302
     :headers {"Location" (str "/notes/" note-id)}
     :body    ""}))

(defn fetch-note-handler
  [_system request]
  (let [note-id (-> request :path-params :note-id)
        note-content (model/get-note note-id)]
    (if note-content
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (pages/note-page note-id note-content)}
      {:status 404
       :headers {"Content-Type" "text/html"}
       :body "Note not found"})))

(defn not-found-handler
  [_request]
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body    "Not Found"})

(defn routes
  [system]
  [
   ["/"
    {:get {:handler (partial #'root-page-handler system)}}]

   ["/create-notes"
    {:get {:handler (partial #'new-note-handler system)}}]

   ["/notes"
    {:post {:handler (partial #'create-note-handler system)}}]

   ["/notes/:note-id"
    {:get {:handler (partial #'fetch-note-handler system)}}]
  ])

(defn root-handler
  [system request]
  (let [handler (reitit-ring/ring-handler
                  (reitit-ring/router (routes system))
                  #'not-found-handler)]
    (handler request)))
