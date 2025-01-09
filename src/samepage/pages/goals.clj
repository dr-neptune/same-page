(ns samepage.pages.goals
  (:require [samepage.pages.layout :as layout]))

(defn goals-table
  [goals]
  (if (empty? goals)
    [:p "No goals yet! Create one below."]
    [:table {:class "min-w-full border border-gray-600 text-left mb-6"}
     [:thead
      [:tr
       [:th {:class "py-2 px-4 border-b border-gray-600"} "Title"]
       [:th {:class "py-2 px-4 border-b border-gray-600"} "Progress (hrs)"]
       [:th {:class "py-2 px-4 border-b border-gray-600"} "Created"]]]
     [:tbody
      (mapcat
       (fn [{:keys [id title target_hours progress_hours created_at]}]
         (let [row-id    (str "goal-" id)
               detail-id (str "goal-detail-" id)
               progress-str (if target_hours
                              (str (or progress_hours 0) " / " target_hours)
                              (str (or progress_hours 0)))]
           [;; Main row
            [:tr {:key row-id
                  ;; toggling code -> see below
                  :onclick (str "toggleGoalRow('" id "');")
                  :class "cursor-pointer hover:bg-[#3b2a40]"}
             ;; Title
             [:td {:class "py-2 px-4 border-b border-gray-600"} title]
             ;; Show "progress_hours / target_hours"
             [:td {:class "py-2 px-4 border-b border-gray-600"} progress-str]
             ;; Created date
             [:td {:class "py-2 px-4 border-b border-gray-600"}
              (layout/format-timestamp created_at)]]

            ;; Hidden detail row
            [:tr {:key detail-id
                  :id  detail-id
                  :data-state "closed"}
             ;; blank cell, we’ll fill it via HTMX or JS
             [:td {:colspan "3"
                   :class "border-b border-gray-600 p-0"} ""]]]))
       goals)]]))

(defn goal-form
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

   ;; NEW: progress_hours
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

(defn goals-page
  "Displays a list of the user's goals plus a form to create one."
  [request goals]
  (let [user (get-in request [:session :user])]
    (layout/page-layout
     request
     "Your Goals"
     [:div {:class "max-w-2xl mx-auto bg-[#2a2136] p-6 rounded shadow-md"}
      [:h1 {:class "text-3xl mb-4 font-bold"} (str "Your Goals, " (:name user))]
      (goals-table goals)
      (goal-form)])))
