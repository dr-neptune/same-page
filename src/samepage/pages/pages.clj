(ns samepage.pages.pages
  (:require [hiccup.page :refer [html5]]
            [samepage.model.model :as model])
  (:import (java.sql Timestamp)
           (java.time.format DateTimeFormatter)
           (java.time ZoneId)))

(set! *warn-on-reflection* true)

;; 1) Format timestamps
(defn format-timestamp
  [^Timestamp ts]
  (let [formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm")
        instant   (.toInstant ts)
        zdt       (.atZone instant (ZoneId/systemDefault))]
    (.format zdt formatter)))

(defn page-layout
  "Common layout. If a user is logged in, display their info in top bar."
  [request title & body-content]
  (let [session    (:session request)
        user       (:user session)
        user-info  (when user
                     [:div {:class "text-right p-2 bg-[#2a2136] mb-6"}  ;; add bottom margin so content isn't flush
                      [:span (str "Logged in as: "
                                  (or (:name user) "???")
                                  " ("
                                  (or (:email user) "???")
                                  ")")]])]
    (html5
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
      [:title title]
      ;; Tailwind:
      [:script {:src "https://cdn.tailwindcss.com"}]
      ;; For partial updates:
      [:script {:src "https://unpkg.com/htmx.org@1.9.2"}]]
     [:body
      {:class "min-h-screen p-8 bg-[#1e1e28] text-[#e0def2]"}
      user-info
      body-content])))

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

(defn note-form []
  [:form {:hx-post "/notes"
          :hx-target "#notes-table"
          :hx-swap "innerHTML"
          :class "mb-6"}
   [:label {:class "block mb-2 font-semibold"} "Note content:"]
   [:textarea {:id "note-text"
               :name "note-text"
               :required true
               :class "w-full h-32 border border-gray-500 rounded p-2 bg-[#2f2b3b] text-[#e0def2]"
               :placeholder "Write your note here..."}]
   [:button {:type "submit"
             :class "mt-4 bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700"}
    "Create Note"]])

(defn root-page
  [request]
  (let [session   (:session request)
        user      (:user session)
        user-name (:name user)
        notes     (when user (model/get-notes-for-user user-name))]
    (page-layout
     request
     (if user (str "Welcome, " user-name) "Home - Mastery App")
     [:div {:class "max-w-2xl mx-auto bg-[#2a2136] p-6 rounded shadow-md"}
      (if-not user
        ;; If no user logged in, prompt to register
        [:div
         [:h1 {:class "text-3xl mb-2"} "Welcome to the 10,000 Hours Mastery App"]
         [:p "Track your deliberate practice across multiple goals."]
         [:div {:class "mt-4"}
          ;; unify button color with purple
          [:a {:href "/register"
               :class "bg-purple-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
           "Register Here"]]]
        ;; If user is logged in, show note form + table
        [:div
         [:h1 {:class "text-3xl mb-4 font-bold"}
          (str "Your Notes, " user-name)]
         (note-form)
         [:div {:id "notes-table"}
          (notes-table notes)]
         ;; Clear the textarea after partial swap
         [:script
          "document.body.addEventListener('htmx:afterSwap', function(evt) {
             if (evt.detail.target.id === 'notes-table') {
               let ta = document.getElementById('note-text');
               if (ta) ta.value = '';
             }
           });"]])])))

(defn register-user-page
  [request error-message]
  (page-layout
   request
   "Register New User"
   [:div {:class "max-w-md mx-auto bg-[#2a2136] p-6 rounded shadow-md"}
    [:h1 {:class "text-3xl mb-4 font-bold"} "Register"]
    (when error-message
      [:div {:class "bg-red-600 text-white p-2 rounded mb-4"}
       error-message])
    [:form {:action "/register" :method "post"
            :class "space-y-4"}
     [:div
      [:label {:class "block font-semibold"} "Name:"]
      [:input {:type "text"
               :name "name"
               :class "w-full p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"
               :required true}]]
     [:div
      [:label {:class "block font-semibold"} "Email:"]
      [:input {:type "email"
               :name "email"
               :class "w-full p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"
               :required true}]]
     [:div
      [:label {:class "block font-semibold"} "Password:"]
      [:input {:type "password"
               :name "password"
               :class "w-full p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"
               :required true}]]
     [:button {:type "submit"
               :class "bg-purple-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
      "Register"]]]))

(defn new-note-page
  [notes user-name]
  (page-layout
   nil
   (str "Notes for " user-name)
   ;; Using wider container + more padding to fill the screen
   [:div {:class "max-w-2xl mx-auto p-8 bg-[#2a2136] rounded shadow-md"}
    [:h1 {:class "text-3xl mb-4 font-bold"}
     (str "Create a New Note (Hi, " user-name ")")]
    (note-form)
    [:div {:id "notes-table"}
     (notes-table notes)]]))
