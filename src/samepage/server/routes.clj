(ns samepage.server.routes
  (:require [reitit.ring :as reitit-ring]
            [hiccup2.core :refer [html]]
            [samepage.pages.pages :as pages]
            [samepage.model.user :as user-model]
            [samepage.model.model :as model]
            [clojure.string :as str]))

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
  (let [params    (:params request)
        name      (get params "name" "")
        email     (get params "email" "")
        password  (get params "password" "")]

    ;; 1) Check blanks
    (cond
      (str/blank? name)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (pages/register-user-page
              request
              "Name is required!")}

      (str/blank? email)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (pages/register-user-page
              request
              "Email is required!")}

      (str/blank? password)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (pages/register-user-page
              request
              "Password is required!")}

      :else
      ;; 2) Check if user with same name or email exists
      (let [existing (user-model/find-by-name-or-email name email)]
        (if existing
          ;; A user row with that name or email => error
          {:status 200
           :headers {"Content-Type" "text/html"}
           :body (pages/register-user-page
                  request
                  (str "A user with name [" name
                       "] or email [" email
                       "] already exists!"))}
          ;; Otherwise create user
          (let [new-user (user-model/create-user! {:name name
                                                   :email email
                                                   :password password})
                session-user (select-keys new-user [:id :name :email :role])]
            {:status  302
             :headers {"Location" "/"}
             :session (assoc (:session request) :user session-user)
             :body    ""}))))))

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

;; -------------- NEW Admin Handler --------------
(defn admin-handler
  [_system request]
  (let [session   (:session request)
        user      (:user session)]
    (if (and user (= "admin" (:role user)))
      (let [all-users (user-model/get-all-users)
            all-notes (model/get-all-notes)]
        {:status 200
         :headers {"Content-Type" "text/html"}
         :body (pages/admin-page request all-users all-notes)})
      ;; Not admin => 404 or redirect
      {:status 404
       :headers {"Content-Type" "text/html"}
       :body "Not Found"})))

(defn not-found-handler
  [_request]
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body    "Not Found"})

(defn get-login-handler
  [_system request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (pages/login-page request nil)})

(defn post-login-handler
  [_system request]
  (let [params   (:params request)
        email    (get params "email" "")
        password (get params "password" "")
        user-row (user-model/find-by-email-and-check email password)]
    (if (nil? user-row)
      ;; No match => re-render login page with error
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (pages/login-page request "Invalid email or password.")}
      ;; Otherwise log them in
      (let [session-user (select-keys user-row [:id :name :email :role])]
        {:status 302
         :headers {"Location" "/"}
         :session (assoc (:session request) :user session-user)
         :body ""}))))

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
   ["/admin"
    {:get {:handler (partial #'admin-handler system)}}]])

(defn root-handler
  [system request]
  (let [handler (reitit-ring/ring-handler
                 (reitit-ring/router (routes system))
                 not-found-handler)]
    (handler request)))
