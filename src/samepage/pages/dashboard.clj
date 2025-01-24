(ns samepage.pages.dashboard
  (:require [samepage.pages.layout :as layout]
            [samepage.pages.notes :as notes]
            [samepage.pages.goals :as goals]
            [samepage.model.notes :as note-model]
            [samepage.model.goal :as goal-model]
            [samepage.model.practicelog :as pl]))

(defn dashboard-page
  [request user]
  (let [display   (or (:display_name user) (:name user))
        user-id   (:id user)
        user-name (:name user)
        user-notes (note-model/get-notes-for-user user-name)
        raw-goals  (goal-model/get-goals-for-user user-id)
        user-goals (map (fn [g]
                          (let [sum-durations (pl/get-total-duration-for-goal (:id g))
                                combined      (+ (or (:progress_hours g) 0) sum-durations)]
                            (assoc g :augmented-progress combined)))
                        raw-goals)]
    (layout/page-layout
     request
     (str "Your Dashboard - " display)
     [:div {:class "max-w-2xl mx-auto bg-[#2a2136] p-6 rounded shadow-md"}

      ;; Top area => user’s profile pic, name
      [:div {:class "flex items-center justify-between mb-4"}
       (if (seq (str (:profile_pic user)))
         [:img {:src   (:profile_pic user)
                :alt   (str display " profile pic")
                :class "w-32 h-32 object-cover rounded-lg border border-gray-500"}]
         ;; fallback if no pic
         [:div {:class "w-32 h-32 bg-gray-600 text-gray-300
                        flex items-center justify-center rounded-lg border border-gray-500"}
          "No pic"])
       [:h1 {:class "text-2xl font-bold"} display]]

      ;; Horizontal rule
      [:hr {:class "my-4 border-gray-500"}]

      ;; ===============================================
      ;; NOTES SECTION
      [:div {:class "flex items-center justify-between mb-2"}
       [:h2 {:class "text-xl font-semibold"} "Your Notes"]
       ;; Create Note button to the right
       [:a {:href "/notes/new"
            :class "bg-purple-600 text-white py-1 px-3 rounded hover:bg-purple-700"}
        "Create Note"]]
      (notes/user-notes-list user-notes)

      ;; ===============================================
      ;; GOALS SECTION
      [:div {:class "flex items-center justify-between mt-8 mb-2"}
       [:h2 {:class "text-xl font-semibold"} "Your Goals"]
       ;; Create Goal button to the right
       [:a {:href "/goals/new"
            :class "bg-purple-600 text-white py-1 px-3 rounded hover:bg-purple-700"}
        "Create Goal"]]
      (goals/user-goals-table user-goals)])))
