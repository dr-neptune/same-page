(ns samepage.pages.goals
  (:require [samepage.pages.layout :as layout]))

(defn goals-table
  "Renders a table of the user's goals."
  [goals]
  (if (empty? goals)
    [:p "No goals yet! Create one below."]
    [:table {:class "min-w-full border border-gray-600 text-left mb-6"}
     [:thead
      [:tr
       [:th {:class "py-2 px-4 border-b border-gray-600"} "Title"]
       [:th {:class "py-2 px-4 border-b border-gray-600"} "Target Hours"]
       [:th {:class "py-2 px-4 border-b border-gray-600"} "Created"]]]
     [:tbody
      (for [{:keys [id title description target_hours created_at]} goals]
        [:tr {:key id :class "hover:bg-[#3b2a40]"}
         [:td {:class "py-2 px-4 border-b border-gray-600"} title]
         [:td {:class "py-2 px-4 border-b border-gray-600"} (or target_hours "—")]
         [:td {:class "py-2 px-4 border-b border-gray-600"} 
          (layout/format-timestamp created_at)]])]]))

(defn goal-form
  "A small form to create a goal. We POST to /goals."
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
