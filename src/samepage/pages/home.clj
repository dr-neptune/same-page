(ns samepage.pages.home
  (:require [samepage.pages.layout :as layout]
            [samepage.pages.notes :as notes]
            [samepage.pages.goals :as goals]
            [samepage.model.model :as model]
            [samepage.model.goal :as goal-model]))

(defn root-page
  [request]
  (let [session   (:session request)
        user      (:user session)
        user-name (:name user)
        user-id   (:id user)
        user-notes (when user (model/get-notes-for-user user-name))
        user-goals (when user (goal-model/get-goals-for-user user-id))]
    (layout/page-layout
     request
     (if user (str "Welcome, " user-name) "Home - Mastery App")
     [:div {:class "max-w-2xl mx-auto bg-[#2a2136] p-6 rounded shadow-md"}

      (if-not user
        ;; ------ If user is NOT logged in ------
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

        ;; ------ Else: user IS logged in ------
        [:div
         ;; Title
         [:h1 {:class "text-3xl mb-4 font-bold"} (str "Your Dashboard, " user-name)]

         ;; NOTES
         [:h2 {:class "text-xl mb-2 font-semibold"} "Your Notes"]
         (notes/note-form)
         [:div {:id "notes-table"}
          (notes/notes-table user-notes)]
         [:script
          "document.body.addEventListener('htmx:afterSwap', function(evt) {
             if (evt.detail.target.id === 'notes-table') {
               let ta = document.getElementById('note-text');
               if (ta) ta.value = '';
             }
           });"]

         ;; GOALS
         [:h2 {:class "text-xl mt-8 mb-2 font-semibold"} "Your Goals"]
         (goals/goals-table user-goals)
         (goals/goal-form)])])))
