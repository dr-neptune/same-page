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
        ;; If user is logged in, choose display_name if non-empty, else name
        display    (when user
                     (if (seq (str (:display_name user)))
                       (:display_name user)
                       (:name user)))
        user-id    (:id user)
        user-name  (:name user)
        user-notes (when user (note-model/get-notes-for-user user-name))
        raw-goals  (when user (goal-model/get-goals-for-user user-id))
        user-goals (when raw-goals
                     (map (fn [g]
                            (let [sum-durations (pl/get-total-duration-for-goal (:id g))
                                  progress-mins (* (or (:progress_hours g) 0) 60)
                                  combined      (+ progress-mins sum-durations)]
                              (assoc g :augmented-progress combined)))
                          raw-goals))]
    (layout/page-layout
     request
     (if user (str "Welcome, " display) "Home - Mastery App")
     [:div {:class "max-w-2xl mx-auto bg-[#2a2136] p-6 rounded shadow-md"}

      (if-not user
        ;; -----------------------------------------------------
        ;; Logged OUT => simple landing
        [:div
         [:h1 {:class "text-3xl mb-2"} "Welcome to the 10,000 Hours Mastery App"]
         [:p "Track your deliberate practice across multiple goals."]
         [:div {:class "mt-4 space-x-4 inline-block"}
          ;; Register
          [:a {:href "/register"
               :class "bg-purple-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
           "Register Here"]
          ;; Login
          [:a {:href "/login"
               :class "bg-purple-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
           "Log In"]

          ;; Quick dev login:
          [:form {:action "/login" :method "post" :class "inline-block ml-2"}
           [:input {:type "hidden" :name "email" :value "old@rottenhat"}]
           [:input {:type "hidden" :name "password" :value "pw"}]
           [:button {:type "submit"
                     :class "bg-gray-500 text-white py-2 px-4 rounded hover:bg-gray-600"}
            "Quick Login as old@rottenhat"]]]]

        ;; -----------------------------------------------------
        ;; Logged IN => show "Your Dashboard"
        [:div
         ;; Top area: big avatar on the left, "Your Dashboard" on right
         [:div {:class "flex items-center justify-between mb-4"}
          (if (seq (str (:profile_pic user)))
            [:img {:src   (:profile_pic user)
                   :alt   "Profile Picture"
                   :class "w-32 h-32 object-cover rounded-lg border border-gray-500"}]
            [:div {:class "w-32 h-32 bg-gray-600 text-gray-300
                           flex items-center justify-center rounded-lg border border-gray-500"}
             "No pic"])
          [:h1 {:class "text-2xl font-bold"}
           [:span "Your Dashboard,"]
           [:span {:class "text-pink-400"} display]]]

         ;; A horizontal rule to separate top from main content
         [:hr {:class "mb-6 border-gray-600"}]

         ;; NOTES
         [:h2 {:class "text-xl mb-2 font-semibold"} "Your Notes"]
         (notes/user-notes-list user-notes)
         [:div {:class "mt-4"}
          [:a {:href "/notes/new"
               :class "bg-purple-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
           "Create a Note"]]

         ;; GOALS
         [:h2 {:class "text-xl mt-8 mb-2 font-semibold"} "Your Goals"]
         (goals/user-goals-table user-goals)
         [:div {:class "mt-2"}
          [:a {:href "/goals/new"
               :class "bg-purple-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
           "Create a Goal"]]])])))

;; The new "public" or "shared" dashboard for a given user record
(defn user-dashboard-page
  [request user-record]
  (let [user-name  (:name user-record)
        user-id    (:id user-record)
        user-notes (note-model/get-notes-for-user user-name)
        raw-goals  (goal-model/get-goals-for-user user-id)
        user-goals (map (fn [g]
                          (let [sum-durations (pl/get-total-duration-for-goal (:id g))
                                combined (+ (or (:progress_hours g) 0) sum-durations)]
                            (assoc g :augmented-progress combined)))
                        raw-goals)]
    (layout/page-layout
     request
     (str "Public Dashboard - " user-name)
     [:div {:class "max-w-2xl mx-auto bg-[#2a2136] p-6 rounded shadow-md"}
      [:h1 {:class "text-3xl mb-4 font-bold"}
       (str user-name "'s Public Dashboard")]

      ;; NOTES
      [:h2 {:class "text-xl mb-2 font-semibold"} (str user-name "'s Notes")]
      (notes/user-notes-list user-notes)

      ;; GOALS
      [:h2 {:class "text-xl mt-8 mb-2 font-semibold"} (str user-name "'s Goals")]
      (goals/user-goals-table user-goals)])))
