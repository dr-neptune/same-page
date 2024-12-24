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
   [:link
    {:href "https://fonts.googleapis.com/css2?family=Raleway:ital,wght@0,100..900;1,100..900&family=VT323&display=swap"
     :rel "stylesheet"}]

   ;; Tailwind + HTMX
   [:script {:src "https://cdn.tailwindcss.com"}]
   [:script {:src "https://unpkg.com/htmx.org@1.9.2"}]))

;; Partial snippet to show notes
(defn notes-table
  [notes]
  [:div {:class "overflow-x-auto mt-6"}
   [:table {:class "min-w-full border border-gray-600 text-left"}
    [:thead
     [:tr
      [:th {:class "py-2 px-4 border-b border-gray-600"} "Note"]
      [:th {:class "py-2 px-4 border-b border-gray-600"} "Timestamp"]]]
    [:tbody
     (for [{:keys [id text timestamp]} notes]
       [:tr {:key id :class "hover:bg-[#3b2a40]"}
        [:td {:class "py-2 px-4 border-b border-gray-600"} text]
        [:td {:class "py-2 px-4 border-b border-gray-600"} (str timestamp)]])]]])

(defn new-note-page
  "Full page that shows the note creation form plus existing notes below."
  [notes]
  (html5
   [:head (base-head)]
   [:body
    {:class "min-h-screen p-8 bg-[#1e1e28] text-[#e0def2]"
     :style "font-family: 'Raleway', sans-serif;"}

    ;; Container
    [:div {:class "max-w-lg mx-auto p-6 rounded shadow-md bg-[#2a2136]"}
     ;; Header
     [:h1 {:class "text-3xl mb-4"
           :style "font-family: 'VT323', monospace; font-weight: 400;"}
      "Create a New Note"]

     ;; HTMX form (no :method "post" attribute => HTMX handles the POST)
     [:form {:hx-post "/notes"
             :hx-target "#notes-table"
             :hx-swap "innerHTML"}
      [:label {:class "block mb-2 font-semibold"} "Note content:"]
      ;; Dark background, light text
      [:textarea {:name "note-text"
                  :class "w-full h-32 border border-gray-500 rounded p-2 "
                  :style "background-color: #1e1e28; color: #e0def2;"
                  :placeholder "Write your note here..."}]
      [:button
       {:type "submit"
        :class "mt-4 bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"}
       "Create Note"]]

     ;; This div is replaced by HTMX upon form submission
     [:div {:id "notes-table"}
      (notes-table notes)]]]))

;; If you no longer need the single-note page, remove it entirely.
(defn note-page
  [note-id note-text]
  ;; (Optional old page)
  (html5
   [:head (base-head)]
   [:body
    {:class "min-h-screen p-8 bg-[#1e1e28] text-[#e0def2]"
     :style "font-family: 'Raleway', sans-serif;"}
    [:div {:class "max-w-lg mx-auto p-6 rounded shadow-md bg-[#2a2136]"}
     [:h1 {:class "text-3xl mb-4"
           :style "font-family: 'VT323', monospace; font-weight: 400;"}
      "Your Note"]
     [:p {:class "whitespace-pre-wrap"} note-text]
     [:div {:class "mt-4"}
      [:a {:href "/create-notes"
           :class "inline-block text-blue-400 hover:text-blue-500"}
       "Create Another Note"]]]]))

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
     [:link
      {:href "https://fonts.googleapis.com/css2?family=Raleway:ital,wght@0,100..900;1,100..900&family=VT323&display=swap"
       :rel "stylesheet"}]
     [:script {:src "https://cdn.tailwindcss.com"}]
     [:script {:src "https://unpkg.com/htmx.org@1.9.2"}])]
   ;; ^ Make sure the above line properly closes ( ) ]
   [:body
    {:class "min-h-screen p-8 bg-[#1e1e28] text-[#e0def2]"
     :style "font-family: 'Raleway', sans-serif;"}
    [:div {:class "max-w-lg mx-auto p-6 rounded shadow-md bg-[#2a2136]"}
     [:h1 {:class "text-3xl mb-4"
           :style "font-family: 'VT323', monospace; font-weight: 400;"}
      "Welcome to SamePage!"]
     [:p "Click below to start creating notes."]
     [:a {:href "/create-notes"
          :class "inline-block mt-4 bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"}
      "Create a New Note"]]]))
