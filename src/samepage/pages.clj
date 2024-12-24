(ns samepage.pages
  (:require [hiccup.page :refer [html5]]))

(defn base-head
  []
  (list
   ;; Basic meta tags
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
   [:title "SamePage - Raleway + VT323"]

   ;; Google Fonts for Raleway (regular text) and VT323 (headers)
   [:link {:rel "preconnect" :href "https://fonts.googleapis.com"}]
   [:link {:rel "preconnect" :href "https://fonts.gstatic.com" :crossorigin ""}]
   [:link {:href "https://fonts.googleapis.com/css2?family=Raleway:ital,wght@0,100..900;1,100..900&family=VT323&display=swap"
           :rel "stylesheet"}]

   ;; Tailwind CSS from CDN
   [:script {:src "https://cdn.tailwindcss.com"}]))

;; -----------------------------------------------------------
;; Page for creating a new note
;; -----------------------------------------------------------
(defn new-note-page
  []
  (html5
    [:head (base-head)]
    ;; Body uses Raleway by default
    [:body {:class "min-h-screen p-8 bg-[#1e1e28] text-[#e0def2]"
            :style "font-family: 'Raleway', sans-serif;"}

     ;; Card container
     [:div {:class "max-w-lg mx-auto p-6 rounded shadow-md bg-[#2a2136]"}
      ;; Header with VT323
      [:h1 {:class "text-3xl mb-4"
            :style "font-family: 'VT323', monospace; font-weight: 400;"}
       "Create a New Note"]

      [:form {:action "/notes" :method "post"}
       [:label {:class "block mb-2 font-semibold"} "Note content:"]
       [:textarea {:name "note-text"
                   :class "w-full h-32 border border-gray-300 rounded p-2 text-black"
                   :placeholder "Write your note here..."}]
       ;; Submit button
       [:button {:type  "submit"
                 :class "mt-4 bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"}
        "Create Note"]]]]))

;; -----------------------------------------------------------
;; Page for showing a note
;; -----------------------------------------------------------
(defn note-page
  [note-id note-text]
  (html5
    [:head (base-head)]
    [:body {:class "min-h-screen p-8 bg-[#1e1e28] text-[#e0def2]"
            :style "font-family: 'Raleway', sans-serif;"}

     [:div {:class "max-w-lg mx-auto p-6 rounded shadow-md bg-[#2a2136]"}
      ;; Header with VT323
      [:h1 {:class "text-3xl mb-4"
            :style "font-family: 'VT323', monospace; font-weight: 400;"}
       "Your Note"]

      [:p {:class "whitespace-pre-wrap"} note-text]

      ;; Link to create another note
      [:div {:class "mt-4"}
       [:a {:href "/create-notes"
            :class "inline-block text-blue-400 hover:text-blue-500"}
        "Create Another Note"]]]]))

;; -----------------------------------------------------------
;; Root page
;; -----------------------------------------------------------
(defn root-page
  []
  (html5
    [:head
     (list
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
      [:title "SamePage - Raleway + VT323"]
      [:link {:rel "preconnect" :href "https://fonts.googleapis.com"}]
      [:link {:rel "preconnect" :href "https://fonts.gstatic.com" :crossorigin ""}]
      [:link {:href "https://fonts.googleapis.com/css2?family=Raleway:ital,wght@0,100..900;1,100..900&family=VT323&display=swap"
              :rel "stylesheet"}]
      [:script {:src "https://cdn.tailwindcss.com"}])]
    [:body {:class "min-h-screen p-8 bg-[#1e1e28] text-[#e0def2]"
            :style "font-family: 'Raleway', sans-serif;"}

     [:div {:class "max-w-lg mx-auto p-6 rounded shadow-md bg-[#2a2136]"}
      ;; Header with VT323
      [:h1 {:class "text-3xl mb-4"
            :style "font-family: 'VT323', monospace; font-weight: 400;"}
       "Welcome to SamePage!"]

      [:p "Click below to start creating notes."]

      ;; Button that links to /create-notes
      [:a {:href "/create-notes"
           :class "inline-block mt-4 bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"}
       "Create a New Note"]]]))
