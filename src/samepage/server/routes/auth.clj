(ns samepage.server.routes.auth
  (:require [clojure.string :as str]
            [hiccup2.core :refer [html]]
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
  (let [params       (:params request)
        raw-name     (get params "name" "")
        display-name (get params "display_name" "")
        email        (get params "email" "")
        password     (get params "password" "")
        ;; enforce lowercase, trim:
        trimmed-name (str/trim raw-name)
        username     (str/lower-case trimmed-name)]

    (cond
      (str/blank? trimmed-name)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (auth/register-user-page request "Username is required!")}

      ;; Check for spaces or invalid chars. For example, let's allow [a-z0-9-_]
      (not (re-matches #"[a-z0-9_-]+" username))
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (auth/register-user-page
              request "Username must be all lowercase letters, numbers, underscores, or dashes (no spaces).")}

      (str/blank? email)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (auth/register-user-page request "Email is required!")}

      (str/blank? password)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (auth/register-user-page request "Password is required!")}

      :else
      (let [existing (user-model/find-by-name-or-email username email)]
        (if existing
          ;; A user with that name or email already exists
          {:status 200
           :headers {"Content-Type" "text/html"}
           :body (auth/register-user-page
                  request
                  (str "A user with name [" username
                       "] or email [" email
                       "] already exists!"))}

          ;; => create new user
          (let [new-user (user-model/create-user!
                          {:name          username
                           :display_name  display-name
                           :email         email
                           :password      password})
                session-user (select-keys new-user
                                          [:id :name :email :role :display_name])]
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
       :body "Invalid email or password."}
      (let [session-user (select-keys user-row
                                      [:id :name :display_name :email :role])]
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
     :body
     (str
       (html
         [:html
          [:head
           [:title "Logged Out"]
           ;; auto-refresh home after 2 seconds:
           [:meta {:http-equiv "refresh" :content "2;url=/"}]
           ;; load Tailwind for nicer styling
           [:script {:src "https://cdn.tailwindcss.com"}]]
          ;; Body w/ a dark background, white text, etc.
          [:body
           {:class "min-h-screen flex flex-col items-center justify-center bg-[#1e1e28] text-[#e0def2]"}
           [:div {:class "max-w-lg mx-auto bg-[#2a2136] p-6 rounded shadow-md text-center"}
            [:h1 {:class "text-3xl mb-2 font-bold"} "You have been logged out."]
            [:p {:class "mb-4"} "Redirecting to the home page shortly..."]
            ;; A small spinning loader for visual feedback:
            [:div {:class "mx-auto animate-spin h-8 w-8 border-4 border-purple-500 border-t-transparent rounded-full"}]]]]))}))
