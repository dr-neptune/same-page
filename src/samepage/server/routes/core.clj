(ns samepage.server.routes.core
  (:require [reitit.ring :as reitit-ring]
            [samepage.server.routes.home :as home]
            [samepage.server.routes.auth :as auth]
            [samepage.server.routes.notes :as notes]
            [samepage.server.routes.goals :as goals]
            [samepage.server.routes.admin :as admin]))

(defn not-found-handler
  [_request]
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body    "Not Found"})

(defn routes
  [system]
  [;; HOME
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

   ;; Goals
   ["/goals"
    {:post {:handler (partial #'goals/create-goal-handler system)}}]
   ["/goals/new"
    {:get {:handler (partial #'goals/get-new-goal-handler system)}}]
   ["/goals/desc/:id"
    {:get {:handler (partial #'goals/get-goal-detail-handler system)}}]

   ;; Admin
   ["/admin"
    {:get {:handler (partial #'admin/admin-handler system)}}]
   ])

;; The top-level ring handler:
(defn root-handler
  [system request]
  (let [handler (reitit-ring/ring-handler
                 (reitit-ring/router (routes system))
                 not-found-handler)]
    (handler request)))
