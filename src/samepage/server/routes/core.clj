(ns samepage.server.routes.core
  (:require [reitit.ring :as reitit-ring]
            [samepage.server.routes.home :as home]
            [samepage.server.routes.auth :as auth]
            [samepage.server.routes.notes :as notes]
            [samepage.server.routes.goals :as goals]
            [samepage.server.routes.profile :as profile]
            [samepage.server.routes.practicelog :as pl]
            [samepage.server.routes.admin :as admin]))

(defn not-found-handler
  [_request]
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body "Not Found"})

(defn routes
  "Defines all of our app routes using Reitit."
  [system]
  [;; 1) Serve a user’s dashboard by name => /:username
   ["/u/:username"
    {:get {:handler (partial #'home/user-dashboard-handler system)}}]
   ["/profile"
    {:get  {:handler (partial #'profile/get-profile-handler system)}
     :post {:handler (partial #'profile/post-profile-handler system)}}]

   ;; 2) Root page => /
   ["/"
    {:get {:handler (partial #'home/root-page-handler system)}}]

   ;; Auth
   ["/register"
    {:get  {:handler (partial #'auth/get-register-handler system)}
     :post {:handler (partial #'auth/post-register-handler system)}}]
   ["/login"
    {:get  {:handler (partial #'auth/get-login-handler system)}
     :post {:handler (partial #'auth/post-login-handler system)}}]
   ["/logout"
    {:get {:handler (partial #'auth/logout-handler system)}}]

   ;; Notes
   ["/notes"
    {:post {:handler (partial #'notes/create-note-handler system)}}]
   ["/notes/new"
    {:get {:handler (partial #'notes/get-new-note-handler system)}}]
   ["/notes/:id/delete"
    {:post {:handler (partial #'notes/delete-note-handler system)}}]
   ["/notes/:id/edit"
    {:get  {:handler (partial #'notes/get-edit-note-handler system)}
     :post {:handler (partial #'notes/post-edit-note-handler system)}}]

   ;; Goals
   ["/goals"
    {:post {:handler (partial #'goals/create-goal-handler system)}}]
   ["/goals/new"
    {:get {:handler (partial #'goals/get-new-goal-handler system)}}]
   ["/goals/:id/desc"
    {:get {:handler (partial #'goals/get-goal-detail-handler system)}}]
   ["/goals/:id/edit"
    {:get  {:handler (partial #'goals/get-edit-goal-handler system)}
     :post {:handler (partial #'goals/post-edit-goal-handler system)}}]
   ["/goals/:id/delete"
    {:post {:handler (partial #'goals/delete-goal-handler system)}}]

   ;; Practice logs
   ["/goals/:goal-id/practice-logs"
    {:get  {:handler (partial #'pl/get-practice-logs-for-goal-handler system)}
     :post {:handler (partial #'pl/post-practice-log-handler system)}}]
   ["/goals/:goal-id/practice-logs/new"
    {:get {:handler (partial #'pl/get-new-practice-log-handler system)}}]

   ;; Admin
   ["/admin"
    {:get {:handler (partial #'admin/admin-handler system)}}]
   ])

(defn root-handler
  "Builds the top-level Ring handler, including a not-found fallback."
  [system request]
  (let [handler (reitit-ring/ring-handler
                 (reitit-ring/router (routes system))
                 not-found-handler)]
    (handler request)))
