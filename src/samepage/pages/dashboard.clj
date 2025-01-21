(ns samepage.pages.dashboard
  (:require [samepage.pages.layout :as layout]
            [samepage.pages.notes :as notes]
            [samepage.pages.goals :as goals]
            [samepage.model.notes :as note-model]
            [samepage.model.goal :as goal-model]
            [samepage.model.practicelog :as pl]))

(defn dashboard-page
  "Shows personal notes & goals for the logged-in user, plus 'Create Note' & 'Create Goal' buttons."
  [request user]
  (let [display    (or (:display_name user) (:name user))
        user-id    (:id user)
        user-name  (:name user)
        user-notes (note-model/get-notes-for-user user-name)
        raw-goals  (goal-model/get-goals-for-user user-id)
        user-goals (map
                     (fn [g]
                       (let [sum-durations (pl/get-total-duration-for-goal (:id g))
                             combined      (+ (or (:progress_hours g) 0) sum-durations)]
                         (assoc g :augmented-progress combined)))
                     raw-goals)]
    (layout/page-layout
     request
     (str "Dashboard - " display)
     [:div {:class "max-w-2xl mx-auto bg-[#2a2136] p-6 rounded shadow-md"}

      ;; Profile header
      [:div {:class "flex items-center justify-between mb-4"}
       (if (seq (str (:profile_pic user)))
         [:img {:src   (:profile_pic user)
                :alt   (str display " profile pic")
                :class "w-32 h-32 object-cover rounded-lg border border-gray-500"}]
         [:div {:class "w-32 h-32 bg-gray-600 text-gray-300
                        flex items-center justify-center rounded-lg border border-gray-500"}
          "No pic"])
       [:h1 {:class "text-2xl font-bold"} display]]

      ;; Create Note & Create Goal
      [:div {:class "flex space-x-4 mb-6"}
       [:a {:href "/notes/new"
            :class "bg-purple-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
        "Create a Note"]
       [:a {:href "/goals/new"
            :class "bg-purple-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
        "Create a Goal"]]

      [:hr {:class "my-4 border-gray-500"}]

      ;; NOTES
      [:h2 {:class "text-xl mb-2 font-semibold"} "Your Notes"]
      (notes/user-notes-list user-notes)

      ;; GOALS
      [:h2 {:class "text-xl mt-8 mb-2 font-semibold"} "Your Goals"]
      (goals/user-goals-table user-goals)])))
