(ns samepage.pages
  (:require [hiccup.page :refer [html5]]))

(defn base-head
  []
  ;; We’ll use a simple list of items, no Hiccup fragments to avoid any stray <<>>.
  (list
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
   [:title "SamePage - Purple Edition"]
   ;; A Google Font: Dancing Script
   [:link {:rel "stylesheet"
           :href "https://fonts.googleapis.com/css2?family=Dancing+Script:wght@600&display=swap"}]
   ;; Include the Tailwind Play CDN
   [:script {:src "https://cdn.tailwindcss.com"}]))

(defn new-note-page
  []
  (html5
    [:head
     (base-head)]
    [:body {:class "bg-[#A25DC7] min-h-screen p-8"
            :style "font-family: 'Dancing Script', cursive;"}
     ;; White card container
     [:div {:class "max-w-lg mx-auto bg-white shadow-md p-6 rounded"}
      [:h1 {:class "text-3xl font-bold mb-4"} "Create a New Note"]
      [:form {:action "/notes" :method "post"}
       [:label {:class "block mb-2 font-semibold"} "Note content:"]
       [:textarea {:name "note-text"
                   :class "w-full h-32 border border-gray-300 rounded p-2"}]
       [:button {:type  "submit"
                 :class "mt-4 bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"}
        "Create Note"]]]]))

(defn note-page
  [note-id note-text]
  (html5
    [:head
     (base-head)]
    [:body {:class "bg-[#A25DC7] min-h-screen p-8"
            :style "font-family: 'Dancing Script', cursive;"}
     [:div {:class "max-w-lg mx-auto bg-white shadow-md p-6 rounded"}
      [:h1 {:class "text-3xl font-bold mb-4"} "Your Note"]
      [:p {:class "text-gray-700 whitespace-pre-wrap"} note-text]
      [:div {:class "mt-4"}
       [:a {:href "/create-notes"
            :class "inline-block text-blue-600 hover:text-blue-800"}
        "Create Another Note"]]]]))

(defn root-page
  []
  (html5
    [:head
     (list
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
      [:title "SamePage Example"]
      [:script {:src "https://cdn.tailwindcss.com"}])]
    [:body {:class "bg-[#A25DC7] min-h-screen p-8"
            :style "font-family: 'Dancing Script', cursive;"}
     [:div {:class "max-w-lg mx-auto bg-white shadow-md p-6 rounded"}
      [:h1 {:class "text-3xl font-bold mb-4"} "Hello from Hiccup!"]
      [:p "This is a minimal example of using Tailwind to style a small part of the page."]
      ;; Example button
      [:button
       {:class "mt-4 bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"}
       "Click Me!"]]]))
