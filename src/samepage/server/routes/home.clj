(ns samepage.server.routes.home
  (:require [samepage.pages.home :as home]
            [samepage.model.user :as user-model]))

(defn root-page-handler
  [_system request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (home/root-page request)})

(defn user-dashboard-handler
  "Handler for GET /u/:username. Tries to find a user by that username
   and display their 'public' or 'shared' dashboard."
  [_system request]
  (let [username (get-in request [:path-params :username])
        user-row (user-model/find-by-name-or-email username username)]
    (cond
      (nil? user-row)
      {:status 404
       :headers {"Content-Type" "text/plain"}
       :body (str "User '" username "' not found!")}

      :else
      ;; Render a page showing the user's notes/goals
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (home/user-dashboard-page request user-row)})))
