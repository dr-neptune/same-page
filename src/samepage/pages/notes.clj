(ns samepage.pages.notes
  (:require [samepage.pages.layout :as layout]))

(defn notes-table
  "Renders a table of notes belonging to a user."
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
         (layout/format-timestamp timestamp)]])]]])

(defn note-form
  "A form to create a new note (HTMX partial update)."
  []
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

(defn new-note-page
  "Optional dedicated page for notes creation."
  [notes user-name]
  (layout/page-layout
   nil
   (str "Notes for " user-name)
   [:div {:class "max-w-2xl mx-auto p-8 bg-[#2a2136] rounded shadow-md"}
    [:h1 {:class "text-3xl mb-4 font-bold"} (str "Create a New Note (Hi, " user-name ")")]
    (note-form)
    [:div {:id "notes-table"}
     (notes-table notes)]]))
