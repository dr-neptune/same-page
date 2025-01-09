(ns samepage.server.routes
  (:require [reitit.ring :as reitit-ring]
            [hiccup2.core :refer [html]]             ;; for partial HTML rendering
            [clojure.string :as str]

            ;; Models
            [samepage.model.user :as user-model]
            [samepage.model.model :as note-model]
            [samepage.model.goal :as goal-model]

            ;; Pages (split by feature)
            [samepage.pages.home :as home]
            [samepage.pages.auth :as auth]
            [samepage.pages.notes :as notes]
            [samepage.pages.goals :as goals]
            [samepage.pages.admin :as admin]))

;; For a 404 fallback
(defn not-found-handler
  [_request]
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body    "Not Found"})

;; ----------------------
;; Root (Home) Page
;; ----------------------
(defn root-page-handler
  [_system request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (home/root-page request)})

;; ----------------------
;; Registration
;; ----------------------
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

    ;; check for blank fields
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
          ;; user row with same name/email => error
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

;; ----------------------
;; Login / Logout
;; ----------------------
(defn get-login-handler
  [_system request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body   (auth/login-page request nil)})

(defn post-login-handler
  [_system request]
  (let [params     (:params request)
        email      (get params "email" "")
        password   (get params "password" "")
        user-row   (user-model/find-by-email-and-check email password)]
    (if (nil? user-row)
      ;; re-render login with error
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
     :body
     (str
       (html
         [:html
          [:head
           [:title "Logged Out"]
           [:meta {:http-equiv "refresh" :content "2;url=/"}]
           [:script {:src "https://cdn.tailwindcss.com"}]]
          [:body
           {:class "min-h-screen flex flex-col items-center justify-center bg-[#1e1e28] text-[#e0def2]"}
           [:div {:class "max-w-lg mx-auto bg-[#2a2136] p-6 rounded shadow-md text-center"}
            [:h1 {:class "text-3xl mb-2 font-bold"} "You have been logged out."]
            [:p {:class "mb-4"} "Redirecting to the home page shortly..."]
            [:div {:class "mx-auto animate-spin h-8 w-8 border-4 border-purple-500 border-t-transparent rounded-full"}]]]]))}))

;; ----------------------
;; Notes
;; ----------------------
(defn create-note-handler
  [_system request]
  (let [session   (:session request)
        user-name (get-in session [:user :name] "Anonymous")
        note-text (get-in request [:params "note-text"] "")]
    (note-model/create-note! user-name note-text)
    (let [notes (note-model/get-notes-for-user user-name)]
      {:status  200
       :headers {"Content-Type" "text/html"}
       :session session
       :body    (str (html (notes/notes-table notes)))})))

(defn new-note-handler
  [_system request]
  (let [session   (:session request)
        user-name (get-in session [:user :name] "Guest")
        notes     (note-model/get-notes-for-user user-name)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (notes/new-note-page notes user-name)}))

;; ----------------------
;; Goals
;; ----------------------
(defn get-goals-handler
  [_system request]
  (let [user-id (get-in request [:session :user :id])]
    (if (nil? user-id)
      {:status 302
       :headers {"Location" "/login"}
       :body ""}
      (let [gs (goal-model/get-goals-for-user user-id)]
        {:status 200
         :headers {"Content-Type" "text/html"}
         :body (goals/goals-page request gs)}))))

(defn post-goals-handler
  [_system request]
  (let [params   (:params request)
        user     (get-in request [:session :user])
        user-id  (:id user)]
    (if (nil? user)
      {:status 302
       :headers {"Location" "/login"}
       :body ""}
      (let [title        (get params "title" "")
            description  (get params "description" "")
            target-hours (some-> (get params "target_hours" "")
                                 not-empty
                                 (Integer/parseInt))]
        (goal-model/create-goal! {:user-id user-id
                                  :title title
                                  :description description
                                  :target_hours target-hours})
        {:status 302
         :headers {"Location" "/"}
         :body ""}))))

;; ----------------------
;; Admin
;; ----------------------
(defn admin-handler
  [_system request]
  (let [session   (:session request)
        user      (:user session)]
    (if (and user (= "admin" (:role user)))
      (let [all-users (user-model/get-all-users)
            all-notes (note-model/get-all-notes)]
        {:status 200
         :headers {"Content-Type" "text/html"}
         :body (admin/admin-page request all-users all-notes)})
      {:status 404
       :headers {"Content-Type" "text/html"}
       :body "Not Found"})))

;; ----------------------
;; The routes
;; ----------------------
(defn routes
  [system]
  [["/"
    {:get {:handler (partial #'root-page-handler system)}}]
   ["/register"
    {:get  {:handler (partial #'get-register-handler system)}
     :post {:handler (partial #'post-register-handler system)}}]
   ["/login"
    {:get  {:handler (partial #'get-login-handler system)}
     :post {:handler (partial #'post-login-handler system)}}]
   ["/logout"
    {:get {:handler (partial #'logout-handler system)}}]
   ["/notes"
    {:post {:handler (partial #'create-note-handler system)}}]
   ["/create-notes"
    {:get {:handler (partial #'new-note-handler system)}}]
   ["/goals"
    {:get  {:handler (partial #'get-goals-handler system)}   ;; optional "GET /goals"
     :post {:handler (partial #'post-goals-handler system)}}]
   ["/admin"
    {:get {:handler (partial #'admin-handler system)}}]])

(defn root-handler
  [system request]
  (let [handler (reitit-ring/ring-handler
                 (reitit-ring/router (routes system))
                 not-found-handler)]
    (handler request)))
