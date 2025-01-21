(ns samepage.server.routes.dashboard
  (:require [samepage.pages.dashboard :as dash]
            [samepage.model.user :as user-model]
            [samepage.pages.home :as home]))

(defn dashboard-handler
  "GET /dashboard => show personal dashboard for the logged-in user."
  [_system request]
  (let [session (:session request)
        user    (:user session)]
    (if (nil? user)
      ;; Not logged in => redirect to /login
      {:status 302
       :headers {"Location" "/login"}
       :body ""}
      ;; Else => show dashboard
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (dash/dashboard-page request user)})))

(defn public-dashboard-handler
  "GET /u/:username => read-only public page for that user."
  [_system request]
  (let [username (get-in request [:path-params :username])
        user-row (user-model/find-by-name-or-email username username)]
    (if (nil? user-row)
      {:status 404
       :headers {"Content-Type" "text/html"}
       :body (str "User '" username "' not found.")}
      ;; Reuse your existing user-dashboard-page from home.clj,
      ;; or create a special read-only version. Here we call the existing:
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (home/user-dashboard-page request user-row)})))
