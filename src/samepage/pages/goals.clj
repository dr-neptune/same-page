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
       [:th {:class "py-2 px-4 border-b border-gray-600"} "Edit"]
       [:th {:class "py-2 px-4 border-b border-gray-600"} "Delete"]]]
     [:tbody
      (mapcat
       (fn [{:keys [id title target_hours progress_hours augmented-progress created_at]}]
         (let [actual-progress (or augmented-progress progress_hours 0)
               progress-str (if target_hours
                              (str actual-progress " / " target_hours)
                              (str actual-progress))]
           [[:tr {:key (str "goal-" id)
                  :onclick (str "toggleGoalRow('" id "');")
                  :data-state "closed"
                  :class "cursor-pointer hover:bg-[#3b2a40]"}
             [:td {:class "py-2 px-4 border-b border-gray-600"} title]
             [:td {:class "py-2 px-4 border-b border-gray-600"} progress-str]
             [:td {:class "py-2 px-4 border-b border-gray-600"}
              (layout/format-timestamp created_at)]
             ;; EDIT button:
             [:td {:class "py-2 px-4 border-b border-gray-600"}
              [:a {:href (str "/goals/" id "/edit")
                   :class "text-blue-500 hover:underline"}
               "✏️"]]
             ;; DELETE button:
             [:td {:class "py-2 px-4 border-b border-gray-600"}
              [:form
               {:action   (str "/goals/" id "/delete")
                :method   "post"
                :onsubmit "return confirm('Are you sure you want to delete this goal?');"}
               [:button {:type  "submit"
                         :class "text-red-500 hover:underline"}
                "🗑️"]]]]
            ;; hidden expansion row
            [:tr {:key (str "goal-detail-" id)
                  :id  (str "goal-detail-" id)
                  :data-state "closed"}
             [:td {:colspan "5"
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
   ;; NEW ICON FIELD
   [:div
    [:label {:class "block font-semibold"} "Tabler Icon Name (optional):"]
    [:input {:type "text"
             :name "icon"
             :placeholder "e.g. music, alarm, guitar, etc."
             :class "w-full p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"}]
    [:p {:class "text-sm text-gray-400"}
     "Enter just the name from the Tabler icon set (e.g. \"music\" => ti ti-music)."]]

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

(defn edit-goal-form
  [goal]
  [:form {:action (str "/goals/" (:id goal) "/edit")
          :method "post"
          :class "space-y-4 mt-4 mb-6"}
   [:div
    [:label {:class "block font-semibold"} "Goal Title:"]
    [:input {:type "text"
             :name "title"
             :value (:title goal)
             :class "w-full p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"
             :required true}]]
   [:div
    [:label {:class "block font-semibold"} "Description:"]
    [:textarea {:name "description"
                :class "w-full h-20 p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"}
     (or (:description goal) "")]]
   [:div
    [:label {:class "block font-semibold"} "Target Hours (optional):"]
    [:input {:type "number"
             :name "target_hours"
             :class "w-32 p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"
             :min "0"
             :value (or (:target_hours goal) 0)}]]
   [:div
    [:label {:class "block font-semibold"} "Progress (hours):"]
    [:input {:type "number"
             :name "progress_hours"
             :class "w-32 p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"
             :min "0"
             :value (or (:progress_hours goal) 0)}]]
   ;; NEW ICON FIELD
   [:div
    [:label {:class "block font-semibold"} "Tabler Icon Name (optional):"]
    [:input {:type "text"
             :name "icon"
             :value (or (:icon goal) "")
             :class "w-full p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"}]
    [:p {:class "text-sm text-gray-400"}
     "Examples: \"music\", \"alarm\", \"guitar\" => uses \"ti ti-music\", etc."]]

   [:button {:type "submit"
             :class "mt-2 bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700"}
    "Update Goal"]])

(defn edit-goal-page
  "Renders a page to edit an existing goal."
  [request goal]
  (layout/page-layout
   request
   (str "Edit Goal: " (:title goal))
   [:div {:class "max-w-md mx-auto bg-[#2a2136] p-6 rounded shadow-md"}
    [:h1 {:class "text-3xl mb-4 font-bold"}
     (str "Edit Goal: " (:title goal))]
    (edit-goal-form goal)]))

;; For the “card style” user table:
(defn- minutes->hhmm [m]
  (let [hrs (quot m 60)
        min (rem m 60)]
    (str hrs "h " min "m")))

(defn- progress-str
  [{:keys [target_hours augmented-progress]}]
  (let [total-mins (or augmented-progress 0)]
    (if target_hours
      (str (minutes->hhmm total-mins) " / " target_hours "h")
      (minutes->hhmm total-mins))))

(defn user-goals-table
  "Render user's goals as cards with icon next to the title.
   If `:read-only? true`, skip edit/delete links."
  ([goals] (user-goals-table goals {}))
  ([goals {:keys [read-only?] :or {read-only? false}}]
   (if (empty? goals)
     [:p "No goals yet!"]
     [:div {:class "space-y-4"}
      (for [{:keys [id title icon] :as goal} goals]
        [:div
         {:key        (str "goal-" id)
          :class      "bg-[#2f2b3b] p-4 rounded-md hover:bg-[#3b2a40] cursor-pointer"
          :onclick    (str "toggleGoalRow('" id "');")
          :data-state "closed"}
         [:div {:class "flex items-center justify-between"}
          ;; Left side => icon & title
          [:div {:class "flex items-center space-x-3"}
           (when (seq icon)
             [:i {:class (str "ti ti-" icon " text-2xl text-purple-300")}])
           [:h3 {:class "text-lg font-bold"} title]]
          ;; Right side => only show Edit/Delete if not read-only
          (when-not read-only?
            [:div
             [:a {:href (str "/goals/" id "/edit")
                  :class "text-blue-400 hover:underline mr-3"}
              "✏️"]
             [:form {:action   (str "/goals/" id "/delete")
                     :method   "post"
                     :onsubmit "return confirm('Delete this goal?');"
                     :class    "inline-block"}
              [:button {:type  "submit"
                        :class "text-red-400 hover:underline"}
               "🗑️"]]])]
         ;; This is the hidden detail area for partial expansions:
         [:div {:id (str "goal-detail-" id)
                :data-state "closed"
                :class "mt-2 hidden"}]])])))
