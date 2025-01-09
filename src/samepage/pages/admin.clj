(ns samepage.pages.admin
  (:require [samepage.pages.layout :as layout]))

(defn admin-table
  "Helper to render a table of data. `rows` is a seq of maps, `columns` is a seq of keys to display."
  [rows columns]
  [:table {:class "min-w-full border border-gray-600 text-left mb-6"}
   [:thead
    [:tr
     (for [col columns]
       [:th {:class "py-2 px-4 border-b border-gray-600"}
        (name col)])]]
   [:tbody
    (for [row rows]
      [:tr {:class "hover:bg-[#3b2a40]"}
       (for [col columns]
         [:td {:class "py-2 px-4 border-b border-gray-600"} 
          (str (get row col))])])]])

(defn admin-page
  "Show all users and notes, plus a link back to root."
  [request users notes]
  (layout/page-layout
   request
   "Admin Panel"
   [:div {:class "max-w-3xl mx-auto bg-[#2a2136] p-6 rounded shadow-md"}
    [:h1 {:class "text-3xl mb-4 font-bold"} "Admin Control Panel"]
    [:p "Here is the current state of each database table."]
    [:h2 {:class "text-xl mt-6 mb-2 font-semibold"} "Users"]
    (admin-table users [:id :name :email :role :created_at :updated_at])
    [:h2 {:class "text-xl mt-6 mb-2 font-semibold"} "Notes"]
    (admin-table notes [:id :user_name :text :created_at])
    ;; link back home
    [:div {:class "mt-6"}
     [:a {:href "/"
          :class "bg-purple-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
      "Back to Home"]]]))
