(ns samepage.server.routes.goals
  (:require [hiccup2.core :refer [html]]
            [clojure.string :as str]
            [samepage.model.goal :as goal-model]
            [samepage.pages.goals :as goal-pages]))

(defn get-new-goal-handler
  [_system request]
  (let [session (:session request)
        user    (:user session)]
    (if user
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (goal-pages/new-goal-page request)}
      {:status 302
       :headers {"Location" "/login"}
       :body ""})))

(defn create-goal-handler
  [_system request]
  (let [params       (:params request)
        user         (get-in request [:session :user])
        user-id      (:id user)]
    (if (nil? user)
      {:status 302 :headers {"Location" "/login"} :body ""}
      (let [title        (get params "title" "")
            description  (get params "description" "")
            target-hours (some-> (get params "target_hours" "")
                                 not-empty
                                 (Integer/parseInt))
            progress-hrs (some-> (get params "progress_hours" "")
                                 not-empty
                                 (Integer/parseInt))]
        (goal-model/create-goal! {:user-id user-id
                                  :title title
                                  :description description
                                  :target_hours target-hours
                                  :progress_hours progress-hrs})
        {:status 302
         :headers {"Location" "/"}
         :body ""}))))

(defn get-goal-detail-handler
  [_system request]
  (let [goal-id (some-> (get-in request [:path-params :id]) (Integer/parseInt))
        goal    (goal-model/get-goal-by-id goal-id)]
    (if (nil? goal)
      {:status 404
       :headers {"Content-Type" "text/plain"}
       :body "Goal not found"}
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str
               (html
                 [:td {:colspan "3" :class "p-4"}
                  [:p [:strong "Description: "] (or (:description goal) "No description.")]
                  [:p (str "Last update: " (:updated_at goal))]]))})))
