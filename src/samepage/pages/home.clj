(ns samepage.pages.home
  (:require [samepage.pages.layout :as layout]
            [samepage.pages.notes :as notes]
            [samepage.pages.goals :as goals]
            [samepage.model.notes :as note-model]
            [samepage.model.goal :as goal-model]
            [samepage.model.practicelog :as pl]))

(defn root-page
  [request]
  (let [session    (:session request)
        user       (:user session)
        user-name  (:name user)
        user-id    (:id user)
        user-notes (when user (note-model/get-notes-for-user user-name))
        raw-goals  (when user (goal-model/get-goals-for-user user-id))
        user-goals (when raw-goals
                     (map (fn [g]
                            (let [sum-durations (pl/get-total-duration-for-goal (:id g))
                                  combined (+ (or (:progress_hours g) 0) sum-durations)]
                              (assoc g :augmented-progress combined)))
                          raw-goals))]
    (layout/page-layout
     request
     (if user (str "Welcome, " user-name) "Home - Mastery App")
     [:div {:class "max-w-2xl mx-auto bg-[#2a2136] p-6 rounded shadow-md"}
      (if-not user
        ;; not logged in
        [:div
         [:h1 {:class "text-3xl mb-2"} "Welcome to the 10,000 Hours Mastery App"]
         [:p "Track your deliberate practice across multiple goals."]
         [:div {:class "mt-4 space-x-4"}
          [:a {:href "/register"
               :class "bg-purple-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
           "Register Here"]
          [:a {:href "/login"
               :class "bg-purple-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
           "Log In"]]]
        ;; else => user is logged in
        [:div
         [:h1 {:class "text-3xl mb-4 font-bold"}
          "Your Dashboard, "
          [:span {:class "text-pink-400"} user-name]]
         ;; NOTES
         [:h2 {:class "text-xl mb-2 font-semibold"} "Your Notes"]
         (notes/notes-table user-notes)
         [:div {:class "mt-2"}
          [:a {:href "/notes/new"
               :class "bg-purple-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
           "Create a Note"]]

         ;; GOALS
         [:h2 {:class "text-xl mt-8 mb-2 font-semibold"} "Your Goals"]
         (goals/goals-table user-goals)
         [:div {:class "mt-2"}
          [:a {:href "/goals/new"
               :class "bg-purple-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
           "Create a Goal"]]])])))
