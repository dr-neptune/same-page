(ns samepage.server.routes
  (:require [reitit.ring :as reitit-ring]
            [samepage.pages.pages :as pages]
            [samepage.model.user :as user-model]
            [samepage.model.model :as model]))

;; GET /
(defn root-page-handler
  [_system request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (pages/root-page request)})

;; GET /register
(defn get-register-handler
  [_system request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (pages/register-user-page request)})

;; POST /register
(defn post-register-handler
  [_system request]
  (let [params   (:params request)
        name     (get params "name")
        email    (get params "email")
        password (get params "password")
        new-user (user-model/create-user! {:name name
                                           :email email
                                           :password password})]
    ;; store minimal info in session
    (let [session-user {:id    (:id new-user)
                        :name  (:name new-user)
                        :email (:email new-user)}]
      {:status  302
       :headers {"Location" "/"}
       :session (assoc (:session request) :user session-user)
       :body    ""})))

;; The older "notes" handlers

(defn create-note-handler
  [_system request]
  (let [session   (:session request)
        user-name (get-in session [:user :name] "Anonymous")
        note-text (get-in request [:params "note-text"] "")]
    (model/create-note! user-name note-text)
    ;; Return updated notes partial
    (let [notes (model/get-notes-for-user user-name)]
      {:status  200
       :headers {"Content-Type" "text/html"}
       :session session
       :body    (str (pages/notes-table notes))})))

;; If you want a dedicated "/create-notes" page (optional):
(defn new-note-handler
  [_system request]
  (let [session   (:session request)
        user-name (get-in session [:user :name] "Guest")
        notes     (model/get-notes-for-user user-name)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (pages/new-note-page notes user-name)}))

(defn not-found-handler
  [_request]
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body "Not Found"})

(defn routes
  [system]
  [["/"
    {:get {:handler (partial #'root-page-handler system)}}]
   ["/register"
    {:get  {:handler (partial #'get-register-handler system)}
     :post {:handler (partial #'post-register-handler system)}}]
   ["/create-notes"
    {:get {:handler (partial #'new-note-handler system)}}]   ;; optional
   ["/notes"
    {:post {:handler (partial #'create-note-handler system)}}]])

(defn root-handler
  [system request]
  (let [handler (reitit-ring/ring-handler
                 (reitit-ring/router (routes system))
                 not-found-handler)]
    (handler request)))
