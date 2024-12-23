(ns samepage.pages
  (:require [hiccup.page :refer [html5 include-js]]))

(defn base-head
  []
  [:<>
   [:meta {:charset "utf-8"}]
   ;; Ensure responsiveness
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1.0"}]
   ;; Title
   [:title "Note"]
   ;; Tailwind Play CDN
   [:script {:src "https://cdn.tailwindcss.com"}]])

(defn new-note-page
  []
  (html5
    [:head
     (base-head)]
    [:body {:class "bg-gray-100 p-8 font-sans"}
     [:div {:class "max-w-lg mx-auto bg-white shadow-md p-6 rounded"}
      [:h1 {:class "text-2xl font-bold mb-4"} "Create a New Note"]
      [:form {:action "/notes" :method "post"}
       [:label {:class "block mb-2 font-semibold"} "Note content:"]
       [:textarea {:name "note-text"
                   :class "w-full h-32 border border-gray-300 rounded p-2"}]
       [:button {:type  "submit"
                 :class "mt-4 bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"}
        "Create Note"]]]]))

(defn note-page
  "Display a page for an existing note."
  [note-id note-text]
  (html5
    [:head
     (base-head)]
    [:body {:class "bg-gray-100 p-8 font-sans"}
     [:div {:class "max-w-lg mx-auto bg-white shadow-md p-6 rounded"}
      [:h1 {:class "text-2xl font-bold mb-4"} "Your Note"]
      [:p {:class "text-gray-700 whitespace-pre-wrap"} note-text]
      [:div {:class "mt-4"}
       [:a {:href "/create-notes"
            :class "inline-block text-blue-600 hover:text-blue-800"}
        "Create Another Note"]]]]))

(defn root-page
  []
  (html5
   [:head
    [:meta {:charset "utf-8"}]
    [:title "SamePage Example"]
    (include-js "https://unpkg.com/htmx.org@1.9.5")]
   [:body
    [:h1 "Hello from Hiccup!"]
    [:p "This is a minimal example of using HTMX to update a small part of the page."]
    [:button
     {:hx-get    "/change-text"
      :hx-target "#demo-target"
      :hx-swap   "innerHTML"}
     "Click Me!"]
    [:div {:id "demo-target"}
     "Original content."]]))
