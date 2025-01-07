(ns samepage.pages.pages
  (:require [hiccup.page :refer [html5]]
            [samepage.model.model :as model])
  (:import (java.sql Timestamp)
           (java.time.format DateTimeFormatter)
           (java.time ZoneId)))

(set! *warn-on-reflection* true)

;; 1) Format timestamps with reflection fixed
(defn format-timestamp
  [^Timestamp ts]
  (let [formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm")
        instant   (.toInstant ts)
        zdt       (.atZone instant (ZoneId/systemDefault))]
    (.format zdt formatter)))

;; 2) Common layout (re-add tailwind link)
(defn page-layout
  "Common layout for all pages. If a user is logged in (found in session),
   display their name/email in a header bar."
  [request title & body-content]
  (let [session (:session request)
        user    (:user session)
        user-info (when user
                    [:div {:class "text-right p-2 bg-[#2a2136]"}
                     [:span (str "Logged in as: " (:name user)
                                 " (" (:email user) ")")]])]
    (html5
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
      [:title title]
      ;; Re-add Tailwind:
      [:script {:src "https://cdn.tailwindcss.com"}]
      ;; htmx (optional for partial updates):
      [:script {:src "https://unpkg.com/htmx.org@1.9.2"}]]
     [:body
      {:class "min-h-screen p-8 bg-[#1e1e28] text-[#e0def2]"}
      user-info
      body-content])))

;; 3) A partial snippet to show a table of notes
(defn notes-table
  [notes]
  [:div {:class "overflow-x-auto mt-6"}
   [:table {:class "min-w-full border border-gray-600 text-left"}
    [:thead
     [:tr
      [:th {:class "py-2 px-4 border-b border-gray-600"} "User"]
      [:th {:class "py-2 px-4 border-b border-gray-600"} "Note"]
      [:th {:class "py-2 px-4 border-b border-gray-600"} "Timestamp"]]]
    [:tbody
     (for [{:keys [id user_name text timestamp]} notes]
       [:tr {:key id :class "hover:bg-[#3b2a40]"}
        [:td {:class "py-2 px-4 border-b border-gray-600"} user_name]
        [:td {:class "py-2 px-4 border-b border-gray-600"} text]
        [:td {:class "py-2 px-4 border-b border-gray-600"}
         (format-timestamp timestamp)]])]]])

;; 4) A snippet for a "create note" form
(defn note-form
  "A reusable HTMX form that posts to /notes and updates #notes-table."
  []
  [:form {:hx-post "/notes"
          :hx-target "#notes-table"
          :hx-swap "innerHTML"}
   [:label {:class "block mb-2 font-semibold"} "Note content:"]
   [:textarea {:id "note-text"
               :name "note-text"
               :class "w-full h-32 border border-gray-500 rounded p-2"
               :style "background-color: #1e1e28; color: #e0def2;"
               :placeholder "Write your note here..."}]
   [:button {:type "submit"
             :class "mt-4 bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700"}
    "Create Note"]])

;; 5) The "root page" (landing). If user is logged in, show notes + form
(defn root-page
  [request]
  (let [session   (:session request)
        user      (:user session)
        user-name (get user :name)
        notes     (when user (model/get-notes-for-user user-name))]
    (page-layout
     request
     (if user (str "Welcome, " user-name) "Home - Mastery App")
     ;; If user is not logged in, show a prompt to register
     (if-not user
       [:div {:class "max-w-lg mx-auto p-4"}
        [:h1 {:class "text-3xl mb-2"} "Welcome to the 10,000 Hours Mastery App"]
        [:p "Track your deliberate practice across multiple goals."]
        [:div {:class "mt-4"}
         [:a {:href "/register"
              :class "bg-blue-600 text-white py-2 px-4 rounded hover:bg-blue-700"}
          "Register Here"]]]
       ;; Else, show their notes plus a form to create new notes
       [:div {:class "max-w-lg mx-auto p-6 bg-[#2a2136] rounded shadow-md"}
        [:h1 {:class "text-3xl mb-4 font-bold"} (str "Your Notes, " user-name)]
        ;; We'll put the form above the table
        (note-form)
        ;; container for partial refresh
        [:div {:id "notes-table"}
         (notes-table notes)]
        ;; A small script to clear the textarea after swap
        [:script
         "document.body.addEventListener('htmx:afterSwap', function(evt) {
            if (evt.detail.target.id === 'notes-table') {
              let ta = document.getElementById('note-text');
              if (ta) ta.value = '';
            }
          });"]]))))

;; 6) A dedicated "register user" page
(defn register-user-page
  [request]
  (page-layout
   request
   "Register New User"
   [:div {:class "max-w-md mx-auto bg-[#2a2136] p-6 rounded shadow-md"}
    [:h1 {:class "text-3xl mb-4 font-bold"} "Register"]
    [:form {:action "/register" :method "post"
            :class "space-y-4"}
     [:div
      [:label {:class "block font-semibold"} "Name:"]
      [:input {:type "text"
               :name "name"
               :class "w-full p-2 border border-gray-300 rounded"}]]
     [:div
      [:label {:class "block font-semibold"} "Email:"]
      [:input {:type "email"
               :name "email"
               :class "w-full p-2 border border-gray-300 rounded"}]]
     [:div
      [:label {:class "block font-semibold"} "Password:"]
      [:input {:type "password"
               :name "password"
               :class "w-full p-2 border border-gray-300 rounded"}]]
     [:button {:type "submit"
               :class "bg-purple-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
      "Register"]]]))

;; 7) The old new-note-page is now mostly replaced by the root page usage,
;;    but if you still want it, you can keep it:
(defn new-note-page
  [notes user-name]
  (page-layout
   nil
   (str "Notes for " user-name)
   [:div {:class "max-w-lg mx-auto p-6 bg-[#2a2136] rounded shadow-md"}
    [:h1 {:class "text-3xl mb-4 font-bold"} (str "Create a New Note (Hi, " user-name ")")]
    (note-form)
    [:div {:id "notes-table"}
     (notes-table notes)]]))
