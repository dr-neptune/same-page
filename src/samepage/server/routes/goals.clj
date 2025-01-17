(ns samepage.server.routes.goals
  (:require [hiccup2.core :refer [html]]
            [clojure.string :as str]
            [samepage.model.goal :as goal-model]
            [samepage.model.practicelog :as pl-model]
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

(defn delete-goal-handler
  [_system request]
  (let [session (:session request)
        user    (:user session)
        goal-id (some-> (get-in request [:path-params :id]) (Integer/parseInt))]
    (if (nil? user)
      {:status 302
       :headers {"Location" "/login"}
       :body ""}
      (let [goal (goal-model/get-goal-by-id goal-id)]
        (cond
          (nil? goal)
          {:status 404 :body "Goal not found."}

          (or (= (:user_id goal) (:id user))
              (= "admin" (:role user)))
          (do
            (goal-model/delete-goal! goal-id)
            {:status 302
             :headers {"Location" "/"}
             :body ""})

          :else
          {:status 403 :body "You do not have permission to delete this goal."})))))

(defn get-goal-detail-handler
  "Expands a goal row, showing the goal's description, last update,
   plus top 5 practice logs + link to see all logs."
  [_system request]
  (let [goal-id (some-> (get-in request [:path-params :id])
                        (Integer/parseInt))
        goal    (goal-model/get-goal-by-id goal-id)]
    (if (nil? goal)
      {:status 404
       :headers {"Content-Type" "text/plain"}
       :body "Goal not found"}
      (let [top-5 (pl-model/get-latest-5-logs-for-goal goal-id)]
        {:status 200
         :headers {"Content-Type" "text/html"}
         :body (str
                (hiccup2.core/html
                 ;; We return a <td> snippet for the expanded row:
                 [:td
                  {:colspan "3"
                   :class "p-4 align-top"}

                  ;; Basic goal info:
                  [:p
                   [:strong "Description: "]
                   (or (:description goal) "No description.")]
                  [:p
                   (str "Last update: " (:updated_at goal))]

                  ;; Link to all practice logs
                  [:div {:class "mt-2"}
                   [:a
                    {:href  (str "/goals/" goal-id "/practice-logs")
                     :class "bg-purple-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
                    "View ALL Practice Logs"]]
                  ;; Divider:
                  [:hr
                   {:class "my-3"}]

                  ;; Up to 5 recent logs
                  [:p
                   {:class "font-bold mb-1"}
                   "Recent Practice Logs:"]
                  (if (empty? top-5)
                    [:p "No logs yet."]
                    [:table
                     {:class "min-w-full border border-gray-600 text-left"}
                     [:thead
                      [:tr
                       [:th
                        {:class "py-1 px-2 border-b border-gray-600"}
                        "Date"]
                       [:th
                        {:class "py-1 px-2 border-b border-gray-600"}
                        "Duration"]
                       [:th
                        {:class "py-1 px-2 border-b border-gray-600"}
                        "Notes"]]]
                     [:tbody
                      (for [{:keys [duration notes practice_date]} top-5]
                        [:tr
                         [:td
                          {:class "py-1 px-2 border-b border-gray-600"}
                          (str practice_date)]
                         [:td
                          {:class "py-1 px-2 border-b border-gray-600"}
                          (str duration)]
                         [:td
                          {:class "py-1 px-2 border-b border-gray-600"}
                          (or notes "")]])]])]))}))))

(defn get-edit-goal-handler
  [_system request]
  (let [session (:session request)
        user    (:user session)
        goal-id (some-> (get-in request [:path-params :id]) (Integer/parseInt))
        goal    (goal-model/get-goal-by-id goal-id)]
    (cond
      (nil? user)
      ;; Not logged in => redirect
      {:status 302 :headers {"Location" "/login"} :body ""}

      (nil? goal)
      ;; No such goal
      {:status 404 :body "Goal not found."}

      (or (= (:user_id goal) (:id user))
          (= "admin" (:role user)))
      ;; The user either owns it or is admin => show edit page
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (goal-pages/edit-goal-page request goal)}

      :else
      ;; Not authorized
      {:status 403 :body "You do not have permission to edit this goal."})))

(defn post-edit-goal-handler
  [_system request]
  (let [session   (:session request)
        user      (:user session)
        goal-id   (some-> (get-in request [:path-params :id]) (Integer/parseInt))
        goal      (goal-model/get-goal-by-id goal-id)
        params    (:params request)]
    (cond
      (nil? user)
      {:status 302 :headers {"Location" "/login"} :body ""}

      (nil? goal)
      {:status 404 :body "Goal not found."}

      (and (not= (:user_id goal) (:id user))
           (not= "admin" (:role user)))
      {:status 403 :body "You do not have permission to edit this goal."}

      :else
      (let [title        (get params "title")
            description  (get params "description")
            target-hours (some-> (get params "target_hours") not-empty Integer/parseInt)
            progress-hrs (some-> (get params "progress_hours") not-empty Integer/parseInt)]
        (goal-model/update-goal! goal-id
                                 {:title          title
                                  :description    description
                                  :target_hours   target-hours
                                  :progress_hours progress-hrs})
        {:status 302
         :headers {"Location" "/"}
         :body ""}))))
