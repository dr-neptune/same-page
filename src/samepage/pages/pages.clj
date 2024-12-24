(ns samepage.pages.pages
  (:require [hiccup.page :refer [html5]]
            [hiccup.core :refer [html]])
  (:import (java.time.format DateTimeFormatter)
           (java.time ZoneId)))

;; 1) Format timestamps
(defn format-timestamp
  [ts]
  (let [formatter (java.time.format.DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm")
        zdt       (-> ts
                      (.toInstant)                 ;; convert java.sql.Timestamp -> java.time.Instant
                      (.atZone (java.time.ZoneId/systemDefault)))]
    (.format zdt formatter)))

;; 2) A reusable layout function so we don't repeat <head> tags every time
(defn page-layout
  [title & body-content]
  (html5
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
    [:title title]
    [:link {:rel "preconnect" :href "https://fonts.googleapis.com"}]
    [:link {:rel "preconnect" :href "https://fonts.gstatic.com" :crossorigin ""}]
    [:link
     {:href "https://fonts.googleapis.com/css2?family=Raleway:ital,wght@0,100..900;1,100..900&family=VT323&display=swap"
      :rel "stylesheet"}]
    [:script {:src "https://cdn.tailwindcss.com"}]
    [:script {:src "https://unpkg.com/htmx.org@1.9.2"}]]
   [:body
    {:class "min-h-screen p-8 bg-[#1e1e28] text-[#e0def2]"
     :style "font-family: 'Raleway', sans-serif;"}
    body-content]))

;; 3) The partial snippet for notes
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

;; 4) The create-note page
(defn new-note-page
  [notes user-name]
  (page-layout
   (str "Notes for " user-name)
   ;; Script to clear the textarea after HTMX updates #notes-table
   [:script
    "document.body.addEventListener('htmx:afterSwap', function(evt) {
       if (evt.detail.target.id === 'notes-table') {
         let ta = document.getElementById('note-text');
         if (ta) ta.value = '';
       }
     });"]
   [:div {:class "max-w-lg mx-auto p-6 rounded shadow-md bg-[#2a2136]"}
    [:h1 {:class "text-3xl mb-4"
          :style "font-family: 'VT323', monospace; font-weight: 400;"}
     (str "Create a New Note (Hi, " user-name ")")]

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
      "Create Note"]]

    [:div {:id "notes-table"}
     (notes-table notes)]]))

;; 5) The registration page
(defn register-page
  []
  (page-layout
   "Register - SamePage"
   [:div {:class "max-w-lg mx-auto p-6 rounded shadow-md bg-[#2a2136]"}
    [:h1 {:class "text-3xl mb-4"
          :style "font-family: 'VT323', monospace; font-weight: 400;"}
     "Register"]
    [:form {:action "/register" :method "post"}
     [:label {:class "block mb-2 font-semibold"} "What's your name?"]
     [:input {:type "text"
              :name "user-name"
              :class "w-full border border-gray-500 rounded p-2 mb-4"
              :style "background-color: #1e1e28; color: #e0def2;"}]
     [:button
      {:type "submit"
       :class "bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700"}
      "Register"]]]))

;; 6) The landing page
(defn root-page
  []
  (page-layout
   "SamePage - Landing"
   [:div {:class "max-w-lg mx-auto p-6 rounded shadow-md bg-[#2a2136]"}
    [:h1 {:class "text-3xl mb-4"
          :style "font-family: 'VT323', monospace; font-weight: 400;"}
     "Welcome to SamePage!"]
    [:p "This is our landing page. Register or create notes!"]

    [:a {:href "/register"
         :class "inline-block mt-4 bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700 mr-4"}
     "Register"]

    [:a {:href "/create-notes"
         :class "inline-block mt-4 bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700"}
     "Create Notes"]]))
