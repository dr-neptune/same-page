(ns samepage.server.routes
  (:require [reitit.ring :as reitit-ring]
            [hiccup2.core :refer [html]]
            [samepage.pages.pages :as pages]
            [samepage.model.user :as user-model]
            [samepage.model.model :as model]))

(defn root-page-handler
  [_system request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (pages/root-page request)})

(defn get-register-handler
  [_system request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (pages/register-user-page request nil)})

(defn post-register-handler
  [_system request]
  (let [params   (:params request)
        name     (get params "name" "")
        email    (get params "email" "")
        password (get params "password" "")]
    (if (or (clojure.string/blank? name)
            (clojure.string/blank? email)
            (clojure.string/blank? password))
      ;; If any field is blank, re-render the register page with an error
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (pages/register-user-page request
                                       "All fields (name, email, password) are required!")}
      (let [new-user (user-model/create-user! {:name name
                                               :email email
                                               :password password})
            session-user {:id    (:id new-user)
                          :name  (:name new-user)
                          :email (:email new-user)}]
        {:status  302
         :headers {"Location" "/"}
         :session (assoc (:session request) :user session-user)
         :body    ""}))))

(defn create-note-handler
  [_system request]
  (let [session   (:session request)
        user-name (get-in session [:user :name] "Anonymous")
        note-text (get-in request [:params "note-text"] "")]
    (model/create-note! user-name note-text)
    (let [notes (model/get-notes-for-user user-name)]
      {:status  200
       :headers {"Content-Type" "text/html"}
       :session session
       :body    (str (html (pages/notes-table notes)))})))

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
   :body    "Not Found"})

(defn routes
  [system]
  [["/"
    {:get {:handler (partial #'root-page-handler system)}}]
   ["/register"
    {:get  {:handler (partial #'get-register-handler system)}
     :post {:handler (partial #'post-register-handler system)}}]
   ["/notes"
    {:post {:handler (partial #'create-note-handler system)}}]
   ["/create-notes"
    {:get {:handler (partial #'new-note-handler system)}}]])

(defn root-handler
  [system request]
  (let [handler (reitit-ring/ring-handler
                 (reitit-ring/router (routes system))
                 not-found-handler)]
    (handler request)))
