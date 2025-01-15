(ns samepage.server.routes.admin
  (:require [samepage.model.user :as user-model]
            [samepage.model.notes :as note-model]
            [samepage.model.goal :as goal-model]
            [samepage.pages.admin :as admin]))

(defn admin-handler
  [_system request]
  (let [session   (:session request)
        user      (:user session)]
    (if (and user (= "admin" (:role user)))
      (let [all-users (user-model/get-all-users)
            all-notes (note-model/get-all-notes)
            all-goals (goal-model/get-all-goals)]
        {:status 200
         :headers {"Content-Type" "text/html"}
         :body (admin/admin-page request all-users all-notes all-goals)})
      {:status 404
       :headers {"Content-Type" "text/html"}
       :body "Not Found"})))
