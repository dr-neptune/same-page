(ns samepage.pages.goals
  (:require [samepage.pages.layout :as layout]
            [hiccup2.core :as hc]))

(defn goals-table
  [goals]
  (if (empty? goals)
    [:p "No goals yet!"]
    [:table {:class "min-w-full border border-gray-600 text-left mb-6"}
     [:thead
      [:tr
       [:th {:class "py-2 px-4 border-b border-gray-600"} "Title"]
       [:th {:class "py-2 px-4 border-b border-gray-600"} "Progress (hrs)"]
       [:th {:class "py-2 px-4 border-b border-gray-600"} "Created"]
       ;; Extra column for delete button
       [:th {:class "py-2 px-4 border-b border-gray-600"} ""]]]
     [:tbody
      (mapcat
       (fn [{:keys [id title target_hours progress_hours augmented-progress created_at]}]
         (let [actual-progress (or augmented-progress progress_hours 0)
               row-id          (str "goal-" id)
               detail-id       (str "goal-detail-" id)
               progress-str    (if target_hours
                                 (str actual-progress " / " target_hours)
                                 (str actual-progress))]
           [[:tr {:key row-id
                  :onclick (str "toggleGoalRow('" id "');")
                  :data-state "closed"
                  :class "cursor-pointer hover:bg-[#3b2a40]"}
             ;; Title
             [:td {:class "py-2 px-4 border-b border-gray-600"} title]
             ;; Progress
             [:td {:class "py-2 px-4 border-b border-gray-600"} progress-str]
             ;; Created
             [:td {:class "py-2 px-4 border-b border-gray-600"}
              (layout/format-timestamp created_at)]
             ;; Delete button
             [:td {:class "py-2 px-4 border-b border-gray-600"}
              [:form
               {:action   (str "/goals/" id "/delete")
                :method   "post"
                :onsubmit "return confirm('Are you sure you want to delete this goal?');"}
               [:button {:type  "submit"
                         :class "text-red-500 hover:underline"}
                "🗑️"]]]]
            ;; The hidden row for expansion:
            [:tr {:key detail-id
                  :id  detail-id
                  :data-state "closed"}
             [:td {:colspan "4"
                   :class "border-b border-gray-600 p-0"} ""]]]))
       goals)]]))

(defn goal-form
  "A form to create a goal. We POST to /goals."
  []
  [:form {:action "/goals" :method "post"
          :class "space-y-4 mt-4 mb-6"}
   [:div
    [:label {:class "block font-semibold"} "Goal Title:"]
    [:input {:type "text"
             :name "title"
             :class "w-full p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"
             :required true}]]
   [:div
    [:label {:class "block font-semibold"} "Description:"]
    [:textarea {:name "description"
                :class "w-full h-20 p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"}]]
   [:div
    [:label {:class "block font-semibold"} "Target Hours (optional):"]
    [:input {:type "number"
             :name "target_hours"
             :class "w-32 p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"
             :min "0"}]]
   [:div
    [:label {:class "block font-semibold"} "Initial Progress (hours):"]
    [:input {:type "number"
             :name "progress_hours"
             :class "w-32 p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"
             :min "0"
             :value "0"}]]
   [:button {:type "submit"
             :class "mt-2 bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700"}
    "Create Goal"]])

(defn new-goal-page
  "A dedicated page for creating a goal."
  [request]
  (layout/page-layout
   request
   "Create a Goal"
   [:div {:class "max-w-md mx-auto bg-[#2a2136] p-6 rounded shadow-md"}
    [:h1 {:class "text-3xl mb-4 font-bold"} "Create a New Goal"]
    (goal-form)]))
