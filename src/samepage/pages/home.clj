(ns samepage.pages.home
  (:require [samepage.pages.layout :as layout]
            [samepage.pages.notes :as notes]
            [samepage.pages.goals :as goals]
            [samepage.model.model :as note-model]
            [samepage.model.goal :as goal-model]))

(defn root-page
  "Dashboard: shows user notes & goals in an expandable table, no creation forms."
  [request]
  (let [session   (:session request)
        user      (:user session)
        user-name (:name user)
        user-id   (:id user)
        user-notes (when user (note-model/get-notes-for-user user-name))
        user-goals (when user (goal-model/get-goals-for-user user-id))]
    (layout/page-layout
     request
     (if user (str "Welcome, " user-name) "Home - Mastery App")
     [:div {:class "max-w-2xl mx-auto bg-[#2a2136] p-6 rounded shadow-md"}

      (if-not user
        ;; Not logged in -> prompt
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

        ;; If logged in -> show just the tables (expandable)
        [:div
         [:h1 {:class "text-3xl mb-4 font-bold"}
          (str "Your Dashboard, " user-name)]

         ;; NOTES table
         [:h2 {:class "text-xl mb-2 font-semibold"} "Your Notes"]
         (notes/notes-table user-notes)

         ;; A link to create a note on a separate page
         [:div {:class "mt-2"}
          [:a {:href "/notes/new"
               :class "bg-purple-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
           "Create a Note"]]

         ;; GOALS table
         [:h2 {:class "text-xl mt-8 mb-2 font-semibold"} "Your Goals"]
         (goals/goals-table user-goals)

         ;; A link to create a goal on a separate page
         [:div {:class "mt-2"}
          [:a {:href "/goals/new"
               :class "bg-purple-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
           "Create a Goal"]]])])))
