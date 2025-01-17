(ns samepage.server.routes.profile
  (:require [samepage.model.user :as user-model]
            [samepage.pages.profile :as p]))

(defn get-profile-handler
  [_system request]
  (let [session (:session request)
        user    (:user session)]
    (if (nil? user)
      ;; Not logged in => redirect to /login
      {:status 302
       :headers {"Location" "/login"}
       :body ""}
      (let [user-id   (:id user)
            user-row  (user-model/find-by-email (:email user))]
        ;; or find-by-id if you prefer:
        ;; (user-model/get-user-by-id user-id)
        {:status 200
         :headers {"Content-Type" "text/html"}
         :body (p/profile-page request user-row)}))))

(defn post-profile-handler
  [_system request]
  (let [session (:session request)
        user    (:user session)]
    (if (nil? user)
      {:status 302 :headers {"Location" "/login"} :body ""}
      (let [params          (:params request)
            new-display    (get params "display_name" "")
            new-profile-pic (get params "profile_pic" "")
            user-id        (:id user)
            updated-user   (user-model/update-user!
                             user-id
                             {:display_name new-display
                              :profile_pic  new-profile-pic})
            session-user   (merge user updated-user)]
        ;; CHANGE the redirect from "/profile" to "/"
        {:status 302
         :headers {"Location" "/"}
         :session (assoc session :user session-user)
         :body ""}))))
