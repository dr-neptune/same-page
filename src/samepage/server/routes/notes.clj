(ns samepage.server.routes.notes
  (:require [hiccup2.core :refer [html]]
            [samepage.model.notes :as note-model]
            [samepage.pages.notes :as notes]))

(defn get-new-note-handler
  [_system request]
  (let [session (:session request)
        user    (:user session)]
    (if user
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (notes/new-note-page request)}
      {:status 302
       :headers {"Location" "/login"}
       :body ""})))

(defn create-note-handler
  [_system request]
  (let [session   (:session request)
        user-name (get-in session [:user :name] "Anonymous")
        note-text (get-in request [:params "note-text"] "")]
    (note-model/create-note! user-name note-text)
    {:status 302
     :headers {"Location" "/"}
     :body ""}))

(defn delete-note-handler
  [_system request]
  (let [session    (:session request)
        user       (:user session)
        note-id    (some-> (get-in request [:path-params :id]) (Integer/parseInt))]
    (if (nil? user)
      ;; Not logged in => redirect
      {:status  302
       :headers {"Location" "/login"}
       :body    ""}
      (let [note (note-model/get-note-by-id note-id)]
        (cond
          (nil? note)
          {:status 404 :body "Note not found."}

          (or (= (:user_name note) (:name user))
              (= "admin" (:role user)))
          (do
            (note-model/delete-note! note-id)
            {:status 302
             :headers {"Location" "/"}
             :body ""})

          :else
          {:status 403 :body "You do not have permission to delete this note."})))))

(defn get-edit-note-handler
  [_system request]
  (let [session (:session request)
        user    (:user session)
        note-id (some-> (get-in request [:path-params :id]) (Integer/parseInt))
        note    (note-model/get-note-by-id note-id)]
    (cond
      (nil? user)
      {:status 302 :headers {"Location" "/login"} :body ""}

      (nil? note)
      {:status 404 :body "Note not found."}

      ;; authorized if note belongs to them or user is admin
      (or (= (:user_name note) (:name user))
          (= "admin" (:role user)))
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (notes/edit-note-page request note)}

      :else
      {:status 403 :body "You do not have permission to edit this note."})))

(defn post-edit-note-handler
  [_system request]
  (let [session   (:session request)
        user      (:user session)
        note-id   (some-> (get-in request [:path-params :id]) (Integer/parseInt))
        note      (note-model/get-note-by-id note-id)
        params    (:params request)]
    (cond
      (nil? user)
      {:status 302 :headers {"Location" "/login"} :body ""}

      (nil? note)
      {:status 404 :body "Note not found."}

      (and (not= (:user_name note) (:name user))
           (not= "admin" (:role user)))
      {:status 403 :body "You do not have permission to edit this note."}

      :else
      (let [new-text (get params "note-text" "")]
        (note-model/update-note! note-id new-text)
        ;; redirect home (or wherever you like):
        {:status 302
         :headers {"Location" "/"}
         :body ""}))))
