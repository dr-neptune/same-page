(ns samepage.server.routes.auth
  (:require [clojure.string :as str]
            [samepage.pages.auth :as auth]
            [samepage.model.user :as user-model]))

;; Registration
(defn get-register-handler
  [_system request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body   (auth/register-user-page request nil)})

(defn post-register-handler
  [_system request]
  (let [params   (:params request)
        name     (get params "name" "")
        email    (get params "email" "")
        password (get params "password" "")]
    (cond
      (str/blank? name)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (auth/register-user-page request "Name is required!")}

      (str/blank? email)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (auth/register-user-page request "Email is required!")}

      (str/blank? password)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (auth/register-user-page request "Password is required!")}

      :else
      (let [existing (user-model/find-by-name-or-email name email)]
        (if existing
          {:status 200
           :headers {"Content-Type" "text/html"}
           :body (auth/register-user-page
                  request
                  (str "A user with name [" name
                       "] or email [" email
                       "] already exists!"))}
          (let [new-user (user-model/create-user! {:name name
                                                   :email email
                                                   :password password})
                session-user (select-keys new-user [:id :name :email :role])]
            {:status  302
             :headers {"Location" "/"}
             :session (assoc (:session request) :user session-user)
             :body    ""}))))))

(defn get-login-handler
  [_system request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body   (auth/login-page request nil)})

(defn post-login-handler
  [_system request]
  (let [params   (:params request)
        email    (get params "email" "")
        password (get params "password" "")
        user-row (user-model/find-by-email-and-check email password)]
    (if (nil? user-row)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (auth/login-page request "Invalid email or password.")}
      (let [session-user (select-keys user-row [:id :name :email :role])]
        {:status 302
         :headers {"Location" "/"}
         :session (assoc (:session request) :user session-user)
         :body    ""}))))

(defn logout-handler
  [_system request]
  (let [session (dissoc (:session request) :user)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :session session
     :body "<html><body>Logged out! (Redirect in 2s)...</body></html>"}))
